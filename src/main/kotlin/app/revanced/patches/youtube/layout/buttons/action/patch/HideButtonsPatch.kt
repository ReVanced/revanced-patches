package app.revanced.patches.youtube.layout.buttons.action.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.action.annotations.HideButtonsCompatibility
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([ResourceMappingPatch::class, LithoFilterPatch::class])
@Name("hide-video-action-buttons")
@Description("Adds the options to hide action buttons under a video.")
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
                        "revanced_hide_like_dislike_button",
                        StringResource("revanced_hide_like_dislike_button_title", "Hide like and dislike buttons"),
                        false,
                        StringResource("revanced_hide_like_dislike_button_summary_on", "Like and dislike buttons are hidden"),
                        StringResource("revanced_hide_like_dislike_button_summary_off", "Like and dislike buttons are shown")
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
                        "revanced_hide_clip_button",
                        StringResource("revanced_hide_clip_button_title", "Hide clip button"),
                        false,
                        StringResource("revanced_hide_clip_button_summary_on", "Clip button is hidden"),
                        StringResource("revanced_hide_clip_button_summary_off", "Clip button is shown"),
                        StringResource("revanced_hide_clip_button_user_dialog_message",
                            "Hiding the clip button might not work reliably. In the case it does not work, it can only be hidden by enabling \\'Hide all other action buttons\\'")
                    ),
                    SwitchPreference(
                        "revanced_hide_action_buttons",
                        StringResource("revanced_hide_action_buttons_title", "Hide all other action buttons"),
                        false,
                        StringResource("revanced_hide_action_buttons_summary_on", "Share, remix, thanks, shop, live chat buttons are hidden"),
                        StringResource("revanced_hide_action_buttons_summary_off", "Share, remix, thanks, shop, live chat buttons are shown")
                    )
                ),
                StringResource("revanced_hide_buttons_summary", "Hide or show buttons under videos")
            )
        )
        return PatchResultSuccess()
    }
}
