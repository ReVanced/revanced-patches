package app.revanced.patches.youtube.layout.hide.floatingmicrophone.patch

import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.hide.floatingmicrophone.annotations.HideFloatingMicrophoneButtonCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
@HideFloatingMicrophoneButtonCompatibility
@Version("0.0.1")
class HideFloatingMicrophoneButtonResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_floating_microphone_button",
                StringResource(
                    "revanced_hide_floating_microphone_button_enabled_title",
                    "Hide floating microphone button"
                ),
                true,
                StringResource("revanced_hide_floating_microphone_button_summary_on", "Microphone button hidden"),
                StringResource("revanced_hide_floating_microphone_button_summary_off", "Microphone button shown")
            )
        )

        fabButtonId = ResourceMappingPatch.resourceMappings.find { it.type == "id" && it.name == "fab" }?.id
            ?: return PatchResultError("Can not find required fab button resource id")
        return PatchResultSuccess()
    }

    internal companion object {
        var fabButtonId: Long = -1
    }
}
