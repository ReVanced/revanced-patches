package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_17_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
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
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            // 19.17+ is not supported.
        ),
    )

    val fullscreenSeekbarThumbnailsMatch by fullscreenSeekbarThumbnailsFingerprint()

    execute {
        if (is_19_17_or_greater) {
            // Give a more informative error, if the user has turned off version checks.
            throw PatchException("'Restore old seekbar thumbnails' cannot be patched to any version after 19.16.39")
        }

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
