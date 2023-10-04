package app.revanced.patches.youtube.layout.hide.floatingmicrophone

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class
    ]
)
object HideFloatingMicrophoneButtonResourcePatch : ResourcePatch() {
    internal var fabButtonId: Long = -1

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_floating_microphone_button",
                StringResource(
                    "revanced_hide_floating_microphone_button_enabled_title",
                    "Hide floating microphone button"
                ),
                StringResource("revanced_hide_floating_microphone_button_summary_on", "Microphone button hidden"),
                StringResource("revanced_hide_floating_microphone_button_summary_off", "Microphone button shown")
            )
        )

        fabButtonId = ResourceMappingPatch.resourceMappings.find { it.type == "id" && it.name == "fab" }?.id
            ?: throw PatchException("Can not find required fab button resource id")
    }
}
