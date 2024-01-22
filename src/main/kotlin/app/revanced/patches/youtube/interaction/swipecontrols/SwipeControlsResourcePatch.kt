package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.impl.InputType
import app.revanced.patches.shared.misc.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.impl.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Patch(
    dependencies = [SettingsPatch::class, AddResourcesPatch::class]
)
internal object SwipeControlsResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                titleKey = "revanced_swipe_controls_preference_screen",
                preferences = setOf(
                    SwitchPreference("revanced_swipe_brightness"),
                    SwitchPreference("revanced_swipe_volume"),
                    SwitchPreference("revanced_swipe_press_to_engage"),
                    SwitchPreference("revanced_swipe_haptic_feedback"),
                    SwitchPreference("revanced_swipe_save_and_restore_brightness"),
                    TextPreference("revanced_swipe_overlay_timeout", InputType.NUMBER),
                    TextPreference("revanced_swipe_text_overlay_size", InputType.NUMBER),
                    TextPreference("revanced_swipe_overlay_background_alpha", InputType.NUMBER),
                    TextPreference("revanced_swipe_threshold", InputType.NUMBER)
                ),
            )
        )

        context.copyResources(
            "swipecontrols",
            ResourceGroup(
                "drawable",
                "ic_sc_brightness_auto.xml",
                "ic_sc_brightness_manual.xml",
                "ic_sc_volume_mute.xml",
                "ic_sc_volume_normal.xml"
            )
        )
    }
}
