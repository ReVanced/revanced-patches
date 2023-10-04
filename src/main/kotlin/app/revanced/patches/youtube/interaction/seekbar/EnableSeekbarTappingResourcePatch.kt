package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [SettingsPatch::class]
)
object EnableSeekbarTappingResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            SwitchPreference(
                "revanced_seekbar_tapping",
                StringResource("revanced_seekbar_tapping_title", "Enable seekbar tapping"),
                StringResource("revanced_seekbar_tapping_summary_on", "Seekbar tapping is enabled"),
                StringResource("revanced_seekbar_tapping_summary_off", "Seekbar tapping is disabled")
            )
        )
    }
}