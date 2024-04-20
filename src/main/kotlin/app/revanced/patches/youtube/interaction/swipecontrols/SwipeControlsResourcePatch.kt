package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Patch(
    dependencies = [SettingsPatch::class, AddResourcesPatch::class],
)
internal object SwipeControlsResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SWIPE_CONTROLS.addPreferences(
            SwitchPreference("revanced_swipe_brightness"),
            SwitchPreference("revanced_swipe_volume"),
            SwitchPreference("revanced_swipe_press_to_engage"),
            SwitchPreference("revanced_swipe_haptic_feedback"),
            SwitchPreference("revanced_swipe_save_and_restore_brightness"),
            SwitchPreference("revanced_swipe_lowest_value_enable_auto_brightness"),
            TextPreference("revanced_swipe_overlay_timeout", inputType = InputType.NUMBER),
            TextPreference("revanced_swipe_text_overlay_size", inputType = InputType.NUMBER),
            TextPreference("revanced_swipe_overlay_background_alpha", inputType = InputType.NUMBER),
            TextPreference("revanced_swipe_threshold", inputType = InputType.NUMBER),
        )

        context.copyResources(
            "swipecontrols",
            ResourceGroup(
                "drawable",
                "revanced_ic_sc_brightness_auto.xml",
                "revanced_ic_sc_brightness_manual.xml",
                "revanced_ic_sc_volume_mute.xml",
                "revanced_ic_sc_volume_normal.xml",
            ),
        )
    }
}
