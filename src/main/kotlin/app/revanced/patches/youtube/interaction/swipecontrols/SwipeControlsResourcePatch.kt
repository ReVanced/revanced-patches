package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Patch(
    dependencies = [SettingsPatch::class]
)
internal object SwipeControlsResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("SwipeControls")
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                "revanced_swipe_controls_preference_screen",
                "revanced_swipe_controls_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_swipe_brightness",
                        "revanced_swipe_brightness_title",
                        "revanced_swipe_brightness_summary_on",
                        "revanced_swipe_brightness_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_swipe_volume",
                        "revanced_swipe_volume_title",
                        "revanced_swipe_volume_summary_on",
                        "revanced_swipe_volume_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_swipe_press_to_engage",
                        "revanced_swipe_press_to_engage_title",
                        "revanced_swipe_press_to_engage_summary_on",
                        "revanced_swipe_press_to_engage_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_swipe_haptic_feedback",
                        "revanced_swipe_haptic_feedback_title",
                        "revanced_swipe_haptic_feedback_summary_on",
                        "revanced_swipe_haptic_feedback_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_swipe_save_and_restore_brightness",
                        "revanced_swipe_save_and_restore_brightness_title",
                        "revanced_swipe_save_and_restore_brightness_summary_on",
                        "revanced_swipe_save_and_restore_brightness_summary_off"
                    ),
                    TextPreference(
                        "revanced_swipe_overlay_timeout",
                        "revanced_swipe_overlay_timeout_title",
                        "revanced_swipe_overlay_timeout_summary",
                        InputType.NUMBER
                    ),
                    TextPreference(
                        "revanced_swipe_text_overlay_size",
                        "revanced_swipe_text_overlay_size_title",
                        "revanced_swipe_text_overlay_size_summary",
                        InputType.NUMBER
                    ),
                    TextPreference(
                        "revanced_swipe_overlay_background_alpha",
                        "revanced_swipe_overlay_background_alpha_title",
                        "revanced_swipe_overlay_background_alpha_summary",
                        InputType.NUMBER
                    ),
                    TextPreference(
                        "revanced_swipe_threshold",
                        "revanced_swipe_threshold_title",
                        "revanced_swipe_threshold_summary",
                        InputType.NUMBER
                    )
                ),
                "revanced_swipe_controls_preference_screen_summary"
            )
        )

        context.copyResources(
            "youtube/swipecontrols",
            ResourceGroup(
                "drawable",
                "revanced_ic_sc_brightness_auto.xml",
                "revanced_ic_sc_brightness_manual.xml",
                "revanced_ic_sc_volume_mute.xml",
                "revanced_ic_sc_volume_normal.xml"
            )
        )
    }
}
