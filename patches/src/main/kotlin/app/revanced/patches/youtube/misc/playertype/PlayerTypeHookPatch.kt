package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/PlayerTypeHookPatch;"

val playerTypeHookPatch = bytecodePatch(
    description = "Hook to get the current player type and video playback state.",
) {
    dependsOn(sharedExtensionPatch, resourceMappingPatch)

    execute {
        playerTypeFingerprint.method.addInstruction(
            0,
            "invoke-static {p1}, $EXTENSION_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V",
        )

        reelWatchPagerFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches.last().index
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstruction(
                    index + 1,
                    "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->onShortsCreate(Landroid/view/View;)V"
                )
            }
        }

        videoStateFingerprint.let {
            it.method.apply {
                val videoStateFieldName = getInstruction<ReferenceInstruction>(
                    it.instructionMatches.last().index
                ).reference

                addInstructions(
                    0,
                    """
                        iget-object v0, p1, $videoStateFieldName  # copy VideoState parameter field
                        invoke-static {v0}, $EXTENSION_CLASS_DESCRIPTOR->setVideoState(Ljava/lang/Enum;)V
                    """
                )
            }
        }
    }
}
