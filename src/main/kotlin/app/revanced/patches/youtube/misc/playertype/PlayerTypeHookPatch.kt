package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

internal const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/PlayerTypeHookPatch;"

@Suppress("unused")
val playerTypeHookPatch = bytecodePatch(
    description = "Hook to get the current player type and video playback state.",
) {
    dependsOn(integrationsPatch)

    val playerTypeFingerprintResult by playerTypeFingerprint()
    val videoStateFingerprintResult by videoStateFingerprint()

    execute {
        playerTypeFingerprintResult.mutableMethod.addInstruction(
            0,
            "invoke-static {p1}, $INTEGRATIONS_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V",
        )

        videoStateFingerprintResult.mutableMethod.apply {
            val endIndex = videoStateFingerprintResult.scanResult.patternScanResult!!.endIndex
            val videoStateFieldName = getInstruction<ReferenceInstruction>(endIndex).reference

            addInstructions(
                0,
                """
                        iget-object v0, p1, $videoStateFieldName  # copy VideoState parameter field
                        invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoState(Ljava/lang/Enum;)V
                    """,
            )
        }
    }
}
