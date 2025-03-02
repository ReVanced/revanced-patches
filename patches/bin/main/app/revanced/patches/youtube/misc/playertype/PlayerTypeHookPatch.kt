package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/PlayerTypeHookPatch;"

val playerTypeHookPatch = bytecodePatch(
    description = "Hook to get the current player type and video playback state.",
) {
    dependsOn(sharedExtensionPatch)

    execute {
        playerTypeFingerprint.method.addInstruction(
            0,
            "invoke-static {p1}, $EXTENSION_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V",
        )

        videoStateFingerprint.method.apply {
            val endIndex = videoStateFingerprint.patternMatch!!.endIndex
            val videoStateFieldName = getInstruction<ReferenceInstruction>(endIndex).reference

            addInstructions(
                0,
                """
                        iget-object v0, p1, $videoStateFieldName  # copy VideoState parameter field
                        invoke-static {v0}, $EXTENSION_CLASS_DESCRIPTOR->setVideoState(Ljava/lang/Enum;)V
                    """,
            )
        }
    }
}
