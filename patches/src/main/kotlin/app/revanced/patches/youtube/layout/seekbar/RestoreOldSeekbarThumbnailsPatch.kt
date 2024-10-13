package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/RestoreOldSeekbarThumbnailsPatch;"

@Suppress("unused")
val restoreOldSeekbarThumbnailsPatch = bytecodePatch(
    name = "Restore old seekbar thumbnails",
    description = "Adds an option to restore the old seekbar thumbnails that appear above the seekbar while seeking instead of fullscreen thumbnails.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val fullscreenSeekbarThumbnailsMatch by fullscreenSeekbarThumbnailsFingerprint()

    execute {
        addResources("youtube", "layout.seekbar.restoreOldSeekbarThumbnailsPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_restore_old_seekbar_thumbnails"),
        )

        fullscreenSeekbarThumbnailsMatch.mutableMethod.apply {
            val moveResultIndex = instructions.lastIndex - 1

            addInstruction(
                moveResultIndex,
                "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->useFullscreenSeekbarThumbnails()Z",
            )
        }
    }
}
