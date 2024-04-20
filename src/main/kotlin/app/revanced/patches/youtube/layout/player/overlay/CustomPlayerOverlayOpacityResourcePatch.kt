package app.revanced.patches.youtube.layout.player.overlay

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [SettingsPatch::class, ResourceMappingPatch::class, AddResourcesPatch::class],
)
internal object CustomPlayerOverlayOpacityResourcePatch : ResourcePatch() {
    internal var scrimOverlayId = -1L

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            TextPreference("revanced_player_overlay_opacity", inputType = InputType.NUMBER),
        )

        scrimOverlayId = ResourceMappingPatch[
            "id",
            "scrim_overlay",
        ]
    }
}
