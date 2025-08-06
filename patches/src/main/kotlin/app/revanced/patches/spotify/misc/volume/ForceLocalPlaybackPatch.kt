package app.revanced.patches.spotify.misc.volume

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.Reference


val forceLocalPlaybackPatch = bytecodePatch(
    name = "Force Local Playback",
    description = "Forces playback to be local by using AudioAttributes instead of remote playback.",
) {
    compatibleWith("com.spotify.music")

    execute {
        val method = forceLocalPlaybackFingerprint.method
        val instructions = method.instructions

        val remoteCallIndex = instructions.indexOfFirst {
            it.opcode == Opcode.INVOKE_VIRTUAL &&
                    it.getReferenceOrNull<MethodReference>()?.name == "setPlaybackToRemote"
        }

        if (remoteCallIndex == -1) error("setPlaybackToRemote call not found")

        val remoteCall = method.getInstruction<FiveRegisterInstruction>(remoteCallIndex)
        val sessionRegister = remoteCall.registerC

        method.removeInstructions(remoteCallIndex, 1)

        val smaliCode = """
            new-instance v1, Landroid/media/AudioAttributes#Builder;
            invoke-direct {v1}, Landroid/media/AudioAttributes#Builder;-><init>()V
            const/4 v2, 0x3
            invoke-virtual {v1, v2}, Landroid/media/AudioAttributes#Builder;->setLegacyStreamType(I)Landroid/media/AudioAttributes#Builder;
            move-result-object v1
            invoke-virtual {v1}, Landroid/media/AudioAttributes#Builder;->build()Landroid/media/AudioAttributes;
            move-result-object v1
            invoke-virtual {p${sessionRegister}, v1}, Landroid/media/session/MediaSession;->setPlaybackToLocal(Landroid/media/AudioAttributes;)V
        """.trimIndent().replace('#', '$')

        method.addInstructions(
            remoteCallIndex, smaliCode
        )
    }
}

inline fun <reified T : Reference> Any.getReferenceOrNull(): T? {
    return (this as? ReferenceInstruction)?.reference as? T
}
