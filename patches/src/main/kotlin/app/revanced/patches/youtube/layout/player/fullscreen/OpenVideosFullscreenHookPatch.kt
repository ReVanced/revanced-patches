package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.shortsplayer.`Open Shorts in regular player`
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OpenVideosFullscreenHookPatch;"

/**
 * Used by both [`Open videos fullscreen`] and [`Open Shorts in regular player`].
 */
internal val openVideosFullscreenHookPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        versionCheckPatch,
    )

    apply {
        var fingerprint: Fingerprint
        var insertIndex: Int

        if (is_19_46_or_greater) {
            fingerprint = openVideosFullscreenPortraitMethod
            insertIndex = fingerprint.indices.first()

            openVideosFullscreenPortraitMethod.let {
                // Remove A/B feature call that forces what this patch already does.
                // Cannot use the A/B flag to accomplish the same goal because 19.50+
                // Shorts fullscreen regular player does not use fullscreen
                // if the player is minimized and it must be forced using other conditional check.
                it.method.insertLiteralOverride(
                    it.indices.last(),
                    false,
                )
            }
        } else {
            fingerprint = openVideosFullscreenPortraitLegacyMethod
            insertIndex = fingerprint.indices.last()
        }

        fingerprint.let {
            it.method.apply {
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
}
