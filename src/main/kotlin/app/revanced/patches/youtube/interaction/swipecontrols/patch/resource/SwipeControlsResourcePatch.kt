package app.revanced.patches.youtube.interaction.swipecontrols.patch.resource

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.interaction.swipecontrols.annotation.SwipeControlsCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.*
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources

@Name("swipe-controls-resource-patch")
@DependsOn([SettingsPatch::class])
@SwipeControlsCompatibility
@Version("0.0.1")
class SwipeControlsResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                "revanced_swipe_controls", StringResource("revanced_swipe_controls_title", "Swipe controls"), listOf(
                    SwitchPreference(
                        "revanced_enable_swipe_brightness",
                        StringResource("revanced_swipe_brightness_enabled_title", "Enable brightness gesture"),
                        true,
                        StringResource("revanced_swipe_brightness_summary_on", "Brightness swipe is enabled"),
                        StringResource("revanced_swipe_brightness_summary_off", "Brightness swipe is disabled")
                    ),
                    SwitchPreference(
                        "revanced_enable_swipe_volume",
                        StringResource("revanced_swipe_volume_enabled_title", "Enable volume gesture"),
                        true,
                        StringResource("revanced_swipe_volume_summary_on", "Volume swipe is enabled"),
                        StringResource("revanced_swipe_volume_summary_off", "Volume swipe is disabled")
                    ),
                    SwitchPreference(
                        "revanced_enable_press_to_swipe",
                        StringResource("revanced_press_to_swipe_enabled_title", "Enable press-to-swipe gesture"),
                        false,
                        StringResource("revanced_press_to_swipe_summary_on", "Press-to-swipe is enabled"),
                        StringResource("revanced_press_to_swipe_summary_off", "Press-to-swipe is disabled")
                    ),
                    SwitchPreference(
                        "revanced_enable_swipe_haptic_feedback",
                        StringResource("revanced_swipe_haptic_feedback_enabled_title", "Enable haptic feedback"),
                        true,
                        StringResource("revanced_swipe_haptic_feedback_summary_on", "Haptic feedback is enabled"),
                        StringResource("revanced_swipe_haptic_feedback_summary_off", "Haptic feedback is disabled")
                    ),
                    TextPreference(
                        "revanced_swipe_overlay_timeout",
                        StringResource("revanced_swipe_overlay_timeout_title", "Swipe overlay timeout"),
                        InputType.NUMBER,
                        "500",
                        StringResource(
                            "revanced_swipe_overlay_timeout_summary",
                            "The amount of milliseconds the overlay is visible"
                        )
                    ),
                    TextPreference(
                        "revanced_swipe_overlay_text_size",
                        StringResource("revanced_swipe_overlay_text_size_title", "Swipe overlay text size"),
                        InputType.NUMBER,
                        "22",
                        StringResource("revanced_swipe_overlay_text_size_summary", "The text size for swipe overlay")
                    ),
                    TextPreference(
                        "revanced_swipe_overlay_background_alpha",
                        StringResource("revanced_swipe_overlay_background_alpha_title", "Swipe background visibility"),
                        InputType.NUMBER,
                        "127",
                        StringResource(
                            "revanced_swipe_overlay_background_alpha_summary",
                            "The visibility of swipe overlay background"
                        )
                    ),
                    TextPreference(
                        "revanced_swipe_magnitude_threshold",
                        StringResource("revanced_swipe_magnitude_threshold_title", "Swipe magnitude threshold"),
                        InputType.NUMBER,
                        "30",
                        StringResource(
                            "revanced_swipe_magnitude_threshold_summary",
                            "The amount of threshold for swipe to occur"
                        )
                    )
                ),
                StringResource("revanced_swipe_controls_summary","Control volume and brightness")
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
        return PatchResultSuccess()
    }
}
