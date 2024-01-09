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
                listOf(
                    SwitchPreference("revanced_swipe_brightness"),
                    SwitchPreference("revanced_swipe_volume"),
                    SwitchPreference("revanced_swipe_press_to_engage"),
                    SwitchPreference("revanced_swipe_haptic_feedback"),
                    SwitchPreference("revanced_swipe_save_and_restore_brightness"),
                    TextPreference("revanced_swipe_overlay_timeout", InputType.NUMBER),
                    TextPreference("revanced_swipe_text_overlay_size", InputType.NUMBER),
                    TextPreference("revanced_swipe_overlay_background_alpha", InputType.NUMBER),
                    TextPreference("revanced_swipe_threshold", InputType.NUMBER)
                )
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
