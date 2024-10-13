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

@Suppress("unused")
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
            "18.32.39",
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

    execute {
        addResources("youtube", "layout.thumbnails.alternativeThumbnailsPatch")

        val entries = "revanced_alt_thumbnail_options_entries"
        val values = "revanced_alt_thumbnail_options_entry_values"
        PreferenceScreen.ALTERNATIVE_THUMBNAILS.addPreferences(
            ListPreference(
                "revanced_alt_thumbnail_home",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            ListPreference(
                "revanced_alt_thumbnail_subscription",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            ListPreference(
                "revanced_alt_thumbnail_library",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            ListPreference(
                "revanced_alt_thumbnail_player",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            ListPreference(
                "revanced_alt_thumbnail_search",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
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
            ListPreference("revanced_alt_thumbnail_stills_time", summaryKey = null),
        )

        addImageUrlHook(EXTENSION_CLASS_DESCRIPTOR)
        addImageUrlSuccessCallbackHook(EXTENSION_CLASS_DESCRIPTOR)
        addImageUrlErrorCallbackHook(EXTENSION_CLASS_DESCRIPTOR)
    }
}
