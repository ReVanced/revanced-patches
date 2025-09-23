package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.imageurlhook.addImageUrlErrorCallbackHook
import app.revanced.patches.youtube.misc.imageurlhook.addImageUrlHook
import app.revanced.patches.youtube.misc.imageurlhook.addImageUrlSuccessCallbackHook
import app.revanced.patches.youtube.misc.imageurlhook.cronetImageUrlHookPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/AlternativeThumbnailsPatch;"

val alternativeThumbnailsPatch = bytecodePatch(
    name = "Alternative thumbnails",
    description = "Adds options to replace video thumbnails using the DeArrow API or image captures from the video.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        navigationBarHookPatch,
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
        addResources("youtube", "layout.thumbnails.alternativeThumbnailsPatch")

        val entries = "revanced_alt_thumbnail_options_entries"
        val values = "revanced_alt_thumbnail_options_entry_values"
        PreferenceScreen.ALTERNATIVE_THUMBNAILS.addPreferences(
            ListPreference(
                key = "revanced_alt_thumbnail_home",
                entriesKey = entries,
                entryValuesKey = values
            ),
            ListPreference(
                key = "revanced_alt_thumbnail_subscription",
                entriesKey = entries,
                entryValuesKey = values
            ),
            ListPreference(
                key = "revanced_alt_thumbnail_library",
                entriesKey = entries,
                entryValuesKey = values
            ),
            ListPreference(
                key = "revanced_alt_thumbnail_player",
                entriesKey = entries,
                entryValuesKey = values
            ),
            ListPreference(
                key = "revanced_alt_thumbnail_search",
                entriesKey = entries,
                entryValuesKey = values
            ),
            NonInteractivePreference(
                "revanced_alt_thumbnail_dearrow_about",
                // Custom about preference with link to the DeArrow website.
                tag = "app.revanced.extension.youtube.settings.preference.AlternativeThumbnailsAboutDeArrowPreference",
                selectable = true,
            ),
            SwitchPreference("revanced_alt_thumbnail_dearrow_connection_toast"),
            TextPreference("revanced_alt_thumbnail_dearrow_api_url"),
            NonInteractivePreference("revanced_alt_thumbnail_stills_about"),
            SwitchPreference("revanced_alt_thumbnail_stills_fast"),
            ListPreference("revanced_alt_thumbnail_stills_time"),
        )

        addImageUrlHook(EXTENSION_CLASS_DESCRIPTOR)
        addImageUrlSuccessCallbackHook(EXTENSION_CLASS_DESCRIPTOR)
        addImageUrlErrorCallbackHook(EXTENSION_CLASS_DESCRIPTOR)
    }
}
