package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.imageurlhook.addImageUrlHook
import app.revanced.patches.youtube.misc.imageurlhook.cronetImageUrlHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/BypassImageRegionRestrictionsPatch;"

val bypassImageRegionRestrictionsPatch = bytecodePatch(
    name = "Bypass image region restrictions",
    description = "Adds an option to use a different host for user avatar and channel images " +
        "and can fix missing images that are blocked in some countries.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        cronetImageUrlHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.thumbnails.bypassImageRegionRestrictionsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_bypass_image_region_restrictions"),
        )

        // A priority hook is not needed, as the image urls of interest are not modified
        // by AlternativeThumbnails or any other patch in this repo.
        addImageUrlHook(EXTENSION_CLASS_DESCRIPTOR)
    }
}
