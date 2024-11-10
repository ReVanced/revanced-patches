package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.fullscreenSeekbarThumbnailsFingerprint
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_17_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/SeekbarThumbnailsPatch;"

@Suppress("unused")
val seekbarThumbnailsPatch = bytecodePatch(
    name = "Seekbar thumbnails",
    description = "Adds an option to use high quality fullscreen seekbar thumbnails. " +
            "Patching 19.16.39 or lower adds an option to restore old seekbar thumbnails.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        )
    )

    execute {
        addResources("youtube", "layout.seekbar.seekbarThumbnailsPatch")

        if (is_19_17_or_greater) {
            PreferenceScreen.SEEKBAR.addPreferences(
                SwitchPreference("revanced_seekbar_thumbnails_high_quality")
            )
        } else {
            PreferenceScreen.SEEKBAR.addPreferences(
                SwitchPreference("revanced_restore_old_seekbar_thumbnails"),
                SwitchPreference(
                    key = "revanced_seekbar_thumbnails_high_quality",
                    summaryOnKey = "revanced_seekbar_thumbnails_high_quality_legacy_summary_on",
                    summaryOffKey = "revanced_seekbar_thumbnails_high_quality_legacy_summary_on"
                )
            )

            fullscreenSeekbarThumbnailsFingerprint.method().apply {
                val moveResultIndex = instructions.lastIndex - 1

                addInstruction(
                    moveResultIndex,
                    "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->useFullscreenSeekbarThumbnails()Z",
                )
            }
        }

        fullscreenSeekbarThumbnailsQualityFingerprint.method().addInstructions(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->useHighQualityFullscreenThumbnails()Z
                move-result v0
                return v0
            """
        )
    }
}
