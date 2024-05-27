package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.playertype.fingerprint.playerTypeFingerprint
import app.revanced.patches.youtube.misc.playertype.fingerprint.videoStateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val playerTypeHookPatch = bytecodePatch(
    description = "Hook to get the current player type and video playback state.",
) {
    dependsOn(integrationsPatch)

    val playerTypeResult by playerTypeFingerprint
    val videoStateResult by videoStateFingerprint

    val integrationsClassDescriptor = "Lapp/revanced/integrations/youtube/patches/PlayerTypeHookPatch;"

    execute {
        playerTypeResult.mutableMethod.addInstruction(
            0,
            "invoke-static {p1}, $integrationsClassDescriptor->setPlayerType(Ljava/lang/Enum;)V",
        )

        videoStateResult.mutableMethod.apply {
            val endIndex = videoStateResult.scanResult.patternScanResult!!.endIndex
            val videoStateFieldName = getInstruction<ReferenceInstruction>(endIndex).reference

            addInstructions(
                0,
                """
                        iget-object v0, p1, $videoStateFieldName  # copy VideoState parameter field
                        invoke-static {v0}, $integrationsClassDescriptor->setVideoState(Ljava/lang/Enum;)V
                    """,
            )
        }
    }
}
