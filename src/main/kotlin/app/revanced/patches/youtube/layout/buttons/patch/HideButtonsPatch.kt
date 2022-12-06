package app.revanced.patches.youtube.layout.buttons.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.layout.buttons.annotations.HideButtonsCompatibility
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference

@Patch
@DependsOn([ResourceMappingPatch::class, LithoFilterPatch::class])
@Name("hide-video-buttons")
@Description("Adds options to hide action buttons under a video.")
@HideButtonsCompatibility
@Version("0.0.1")
class HideButtonsPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_hide_buttons",
                StringResource("revanced_hide_buttons_title", "Hide action buttons"),
                listOf(
                    SwitchPreference(
                        "revanced_hide_like_button",
                        StringResource("revanced_hide_like_button_title", "Hide like button"),
                        false,
                        StringResource("revanced_hide_like_button_summary_on", "Like button is hidden"),
                        StringResource("revanced_hide_like_button_summary_off", "Like button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_dislike_button",
                        StringResource("revanced_hide_dislike_button_title", "Hide dislike button"),
                        false,
                        StringResource("revanced_hide_dislike_button_summary_on", "Dislike button is hidden"),
                        StringResource("revanced_hide_dislike_button_summary_off", "Dislike button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_download_button",
                        StringResource("revanced_hide_download_button_title", "Hide download button"),
                        false,
                        StringResource("revanced_hide_download_button_summary_on", "Download button is hidden"),
                        StringResource("revanced_hide_download_button_summary_off", "Download button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_playlist_button",
                        StringResource("revanced_hide_playlist_button_title", "Hide playlist button"),
                        false,
                        StringResource("revanced_hide_playlist_button_summary_on", "Playlist button is hidden"),
                        StringResource("revanced_hide_playlist_button_summary_off", "Playlist button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_action_button",
                        StringResource("revanced_hide_action_button_title", "Hide create, clip and thanks buttons"),
                        true,
                        StringResource("revanced_hide_action_button_summary_on", "Buttons are hidden"),
                        StringResource("revanced_hide_action_button_summary_off", "Buttons are shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_share_button",
                        StringResource("revanced_hide_share_button_title", "Hide share button"),
                        false,
                        StringResource("revanced_hide_share_button_summary_on", "Share button is hidden"),
                        StringResource("revanced_hide_share_button_summaryoff", "Share button is shown")
                    ),
                ),
                StringResource("revanced_hide_buttons_summary", "Hide or show buttons under videos")
            )
        )
        return PatchResultSuccess()
    }
}
