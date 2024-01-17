package app.revanced.patches.youtube.layout.buttons.action

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Hide video action buttons",
    description = "Adds options to hide action buttons (such as the Download button) under videos.",
    dependencies = [
        ResourceMappingPatch::class,
        LithoFilterPatch::class
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
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.34"
            ]
        )
    ]
)
@Suppress("unused")
object HideButtonsPatch : ResourcePatch() {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/ButtonsFilter;"

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_hide_buttons_preference_screen",
                StringResource("revanced_hide_buttons_preference_screen_title", "Hide action buttons"),
                listOf(
                    SwitchPreference(
                        "revanced_hide_like_dislike_button",
                        StringResource("revanced_hide_like_dislike_button_title", "Hide like and dislike buttons"),
                        StringResource("revanced_hide_like_dislike_button_summary_on", "Like and dislike buttons are hidden"),
                        StringResource("revanced_hide_like_dislike_button_summary_off", "Like and dislike buttons are shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_live_chat_button",
                        StringResource("revanced_hide_live_chat_button_title", "Hide live chat button"),
                        StringResource("revanced_hide_live_chat_button_summary_on", "Live chat button is hidden"),
                        StringResource("revanced_hide_live_chat_button_summary_off", "Live chat button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_share_button",
                        StringResource("revanced_hide_share_button_title", "Hide share button"),
                        StringResource("revanced_hide_share_button_summary_on", "Share button is hidden"),
                        StringResource("revanced_hide_share_button_summary_off", "Share button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_report_button",
                        StringResource("revanced_hide_report_button_title", "Hide report button"),
                        StringResource("revanced_hide_report_button_summary_on", "Report button is hidden"),
                        StringResource("revanced_hide_report_button_summary_off", "Report button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_remix_button",
                        StringResource("revanced_hide_remix_button_title", "Hide remix button"),
                        StringResource("revanced_hide_remix_button_summary_on", "Remix button is hidden"),
                        StringResource("revanced_hide_remix_button_summary_off", "Remix button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_download_button",
                        StringResource("revanced_hide_download_button_title", "Hide download button"),
                        StringResource("revanced_hide_download_button_summary_on", "Download button is hidden"),
                        StringResource("revanced_hide_download_button_summary_off", "Download button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_thanks_button",
                        StringResource("revanced_hide_thanks_button_title", "Hide thanks button"),
                        StringResource("revanced_hide_thanks_button_summary_on", "Thanks button is hidden"),
                        StringResource("revanced_hide_thanks_button_summary_off", "Thanks button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_clip_button",
                        StringResource("revanced_hide_clip_button_title", "Hide clip button"),
                        StringResource("revanced_hide_clip_button_summary_on", "Clip button is hidden"),
                        StringResource("revanced_hide_clip_button_summary_off", "Clip button is shown"),
                    ),
                    SwitchPreference(
                        "revanced_hide_playlist_button",
                        StringResource("revanced_hide_playlist_button_title", "Hide save to playlist button"),
                        StringResource("revanced_hide_playlist_button_summary_on", "Save button is hidden"),
                        StringResource("revanced_hide_playlist_button_summary_off", "Save button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shop_button",
                        StringResource("revanced_hide_shop_button_title", "Hide shop button"),
                        StringResource("revanced_hide_shop_button_summary_on", "Shop button is hidden"),
                        StringResource("revanced_hide_shop_button_summary_off", "Shop button is shown")
                    )
                ),
                StringResource("revanced_hide_buttons_preference_screen_summary", "Hide or show buttons under videos")
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
