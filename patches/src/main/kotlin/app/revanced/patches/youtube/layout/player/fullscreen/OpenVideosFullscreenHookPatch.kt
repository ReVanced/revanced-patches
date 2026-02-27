package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.CompositeMatch
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.shortsplayer.openShortsInRegularPlayerPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OpenVideosFullscreenHookPatch;"

/**
 * Used by both [openVideosFullscreenPatch] and [openShortsInRegularPlayerPatch].
 */

internal val openVideosFullscreenHookPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        versionCheckPatch,
    )

    apply {
        var match: CompositeMatch
        var insertIndex: Int

        if (is_19_46_or_greater) {
            match = openVideosFullscreenPortraitMethodMatch
            insertIndex = match[0]

            openVideosFullscreenPortraitMethodMatch.let {
                // Remove A/B feature call that forces what this patch already does.
                // Cannot use the A/B flag to accomplish the same goal because 19.50+
                // Shorts fullscreen regular player does not use fullscreen
                // if the player is minimized and it must be forced using other conditional check.
                it.method.insertLiteralOverride(
                    it[-1],
                    false,
                )
            }
        } else {
            match = openVideosFullscreenPortraitLegacyMethodMatch
            insertIndex = match[-1]
        }

        match.method.apply {
            val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

            addInstructions(
                insertIndex + 1,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->doNotOpenVideoFullscreenPortrait(Z)Z
                    move-result v$register
                """,
            )
        }
    }
}
