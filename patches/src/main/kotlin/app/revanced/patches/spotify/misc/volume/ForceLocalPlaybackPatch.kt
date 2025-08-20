package app.revanced.patches.spotify.misc.volume

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

val forceLocalPlaybackPatch = bytecodePatch(
    name = "Force Local Playback",
    description = "Restores the use of the device volume control to change the device music volume.  If you want the device controls to change the volume of a cast device then this patch should not be applied.",
) {
    compatibleWith("com.spotify.music")
    execute {
        forceLocalPlaybackFingerprint.method.apply {
            val remoteCallIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                        getReference<MethodReference>()?.name == "setPlaybackToRemote"
            }

            val sessionRegister = getInstruction<FiveRegisterInstruction>(remoteCallIndex).registerC
            val builderRegister = findFreeRegister(remoteCallIndex, sessionRegister)
            val streamTypeRegister = findFreeRegister(remoteCallIndex, sessionRegister, builderRegister)

            removeInstruction(remoteCallIndex)

            addInstructions(remoteCallIndex, """
                new-instance v$builderRegister, Landroid/media/AudioAttributes${'$'}Builder;
                invoke-direct { v$builderRegister }, Landroid/media/AudioAttributes${'$'}Builder;-><init>()V
                const/4 v$streamTypeRegister, 0x3
                invoke-virtual { v$builderRegister, v$streamTypeRegister }, Landroid/media/AudioAttributes${'$'}Builder;->setLegacyStreamType(I)Landroid/media/AudioAttributes${'$'}Builder;
                move-result-object v$builderRegister
                invoke-virtual { v$builderRegister }, Landroid/media/AudioAttributes${'$'}Builder;->build()Landroid/media/AudioAttributes;
                move-result-object v$builderRegister
                invoke-virtual { v$sessionRegister, v$builderRegister }, Landroid/media/session/MediaSession;->setPlaybackToLocal(Landroid/media/AudioAttributes;)V
            """)

        }
    }
}
