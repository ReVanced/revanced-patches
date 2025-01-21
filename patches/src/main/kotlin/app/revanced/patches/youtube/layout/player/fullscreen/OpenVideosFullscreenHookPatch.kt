package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.shortsplayer.openShortsInRegularPlayerPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OpenVideosFullscreenHookPatch;"

/**
 * Used by both [openVideosFullscreenPatch] and [openShortsInRegularPlayerPatch].
 */
internal val openVideosFullscreenHookPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        versionCheckPatch
    )

    execute {
        var fingerprint: Fingerprint
        var insertIndex: Int

        if (is_19_46_or_greater) {
            fingerprint = openVideosFullscreenPortraitFingerprint
            insertIndex = fingerprint.instructionMatches.first().index

            openVideosFullscreenPortraitFingerprint.let {
                it.method.apply {
                    // Remove A/B feature call that forces what this patch already does.
                    // Cannot use the A/B flag to accomplish the same goal because 19.50+
                    // Shorts fullscreen regular player does not use fullscreen
                    // if the player is minimized and it must be forced using other conditional check.
                    val featureFlagIndex = it.instructionMatches.last().index
                    val featureFlagRegister = getInstruction<OneRegisterInstruction>(featureFlagIndex).registerA

                    addInstruction(featureFlagIndex + 1, "const/4 v$featureFlagRegister, 0x0")
                }
            }
        } else {
            fingerprint = openVideosFullscreenPortraitLegacyFingerprint
            insertIndex = fingerprint.instructionMatches.last().index
        }

        fingerprint.let {
            it.method.apply {
                val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->doNotOpenVideoFullscreenPortrait(Z)Z
                        move-result v$register
                    """
                )
            }
        }
    }
}
