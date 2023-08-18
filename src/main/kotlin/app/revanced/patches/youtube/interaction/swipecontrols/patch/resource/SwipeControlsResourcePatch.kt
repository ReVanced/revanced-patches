package app.revanced.patches.youtube.interaction.swipecontrols.patch.resource

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources

@DependsOn([SettingsPatch::class])
class SwipeControlsResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                "revanced_swipe_controls_preference_screen",
                StringResource("revanced_swipe_controls_preference_screen_title", "Swipe controls"),
                listOf(
                    SwitchPreference(
                        "revanced_swipe_brightness",
                        StringResource("revanced_swipe_brightness_title", "Enable brightness gesture"),
                        StringResource("revanced_swipe_brightness_summary_on", "Brightness swipe is enabled"),
                        StringResource("revanced_swipe_brightness_summary_off", "Brightness swipe is disabled")
                    ),
                    SwitchPreference(
                        "revanced_swipe_volume",
                        StringResource("revanced_swipe_volume_title", "Enable volume gesture"),
                        StringResource("revanced_swipe_volume_summary_on", "Volume swipe is enabled"),
                        StringResource("revanced_swipe_volume_summary_off", "Volume swipe is disabled")
                    ),
                    SwitchPreference(
                        "revanced_swipe_press_to_engage",
                        StringResource("revanced_swipe_press_to_engage_title", "Enable press-to-swipe gesture"),
                        StringResource("revanced_swipe_press_to_engage_summary_on", "Press-to-swipe is enabled"),
                        StringResource("revanced_swipe_press_to_engage_summary_off", "Press-to-swipe is disabled")
                    ),
                    SwitchPreference(
                        "revanced_swipe_haptic_feedback",
                        StringResource("revanced_swipe_haptic_feedback_title", "Enable haptic feedback"),
                        StringResource("revanced_swipe_haptic_feedback_summary_on", "Haptic feedback is enabled"),
                        StringResource("revanced_swipe_haptic_feedback_summary_off", "Haptic feedback is disabled")
                    ),
                    SwitchPreference(
                        "revanced_swipe_save_and_restore_brightness",
                        StringResource("revanced_swipe_save_and_restore_brightness_title", "Save and restore brightness"),
                        StringResource(
                            "revanced_swipe_save_and_restore_brightness_summary_on",
                            "Save and restore brightness when exiting or entering fullscreen"
                        ),
                        StringResource(
                            "revanced_swipe_save_and_restore_brightness_summary_off",
                            "Do not save and restore brightness when exiting or entering fullscreen"
                        )
                    ),
                    TextPreference(
                        "revanced_swipe_overlay_timeout",
                        StringResource("revanced_swipe_overlay_timeout_title", "Swipe overlay timeout"),
                        StringResource(
                            "revanced_swipe_overlay_timeout_summary",
                            "The amount of milliseconds the overlay is visible"
                        ),
                        InputType.NUMBER
                    ),
                    TextPreference(
                        "revanced_swipe_text_overlay_size",
                        StringResource("revanced_swipe_text_overlay_size_title", "Swipe overlay text size"),
                        StringResource("revanced_swipe_text_overlay_size_summary", "The text size for swipe overlay"),
                        InputType.NUMBER
                    ),
                    TextPreference(
                        "revanced_swipe_overlay_background_alpha",
                        StringResource("revanced_swipe_overlay_background_alpha_title", "Swipe background visibility"),
                        StringResource(
                            "revanced_swipe_overlay_background_alpha_summary",
                            "The visibility of swipe overlay background"
                        ),
                        InputType.NUMBER
                    ),
                    TextPreference(
                        "revanced_swipe_threshold",
                        StringResource("revanced_swipe_threshold_title", "Swipe magnitude threshold"),
                        StringResource(
                            "revanced_swipe_threshold_summary",
                            "The amount of threshold for swipe to occur"
                        ),
                        InputType.NUMBER
                    )
                ),
                StringResource("revanced_swipe_controls_preference_screen_summary","Control volume and brightness")
            )
        )

        context.copyResources(
            "swipecontrols",
            ResourceUtils.ResourceGroup(
                "drawable",
                "ic_sc_brightness_auto.xml",
                "ic_sc_brightness_manual.xml",
                "ic_sc_volume_mute.xml",
                "ic_sc_volume_normal.xml"
            )
        )
    }
}
