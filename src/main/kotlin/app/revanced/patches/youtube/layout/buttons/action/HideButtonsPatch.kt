package app.revanced.patches.youtube.layout.buttons.action

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Hide video action buttons",
    description = "Adds options to hide action buttons under a video.",
    dependencies = [
        ResourceMappingPatch::class,
        LithoFilterPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.41",
                "18.45.43"
            ]
        )
    ]
)
@Suppress("unused")
object HideButtonsPatch : ResourcePatch() {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/components/ButtonsFilter;"

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("HideButtons")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_hide_buttons_preference_screen",
                "revanced_hide_buttons_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_hide_like_dislike_button",
                        "revanced_hide_like_dislike_button_title",
                        "revanced_hide_like_dislike_button_summary_on",
                        "revanced_hide_like_dislike_button_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_live_chat_button",
                        "revanced_hide_live_chat_button_title",
                        "revanced_hide_live_chat_button_summary_on",
                        "revanced_hide_live_chat_button_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_share_button",
                        "revanced_hide_share_button_title",
                        "revanced_hide_share_button_summary_on",
                        "revanced_hide_share_button_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_report_button",
                        "revanced_hide_report_button_title",
                        "revanced_hide_report_button_summary_on",
                        "revanced_hide_report_button_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_remix_button",
                        "revanced_hide_remix_button_title",
                        "revanced_hide_remix_button_summary_on",
                        "revanced_hide_remix_button_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_download_button",
                        "revanced_hide_download_button_title",
                        "revanced_hide_download_button_summary_on",
                        "revanced_hide_download_button_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_thanks_button",
                        "revanced_hide_thanks_button_title",
                        "revanced_hide_thanks_button_summary_on",
                        "revanced_hide_thanks_button_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_clip_button",
                        "revanced_hide_clip_button_title",
                        "revanced_hide_clip_button_summary_on",
                        "revanced_hide_clip_button_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_playlist_button",
                        "revanced_hide_playlist_button_title",
                        "revanced_hide_playlist_button_summary_on",
                        "revanced_hide_playlist_button_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_shop_button",
                        "revanced_hide_shop_button_title",
                        "revanced_hide_shop_button_summary_on",
                        "revanced_hide_shop_button_summary_off",
                    )
                ),
                "revanced_hide_buttons_preference_screen_summary"
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
