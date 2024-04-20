package app.revanced.patches.youtube.layout.hide.floatingmicrophone

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    ],
)
internal object HideFloatingMicrophoneButtonResourcePatch : ResourcePatch() {
    internal var fabButtonId: Long = -1

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_hide_floating_microphone_button"),
        )

        fabButtonId = ResourceMappingPatch["id", "fab"]
    }
}
