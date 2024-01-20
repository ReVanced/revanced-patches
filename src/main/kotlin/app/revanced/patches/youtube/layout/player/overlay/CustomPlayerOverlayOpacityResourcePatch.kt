package app.revanced.patches.youtube.layout.player.overlay

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.impl.InputType
import app.revanced.patches.shared.misc.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [SettingsPatch::class, ResourceMappingPatch::class]
)
internal object CustomPlayerOverlayOpacityResourcePatch : ResourcePatch() {
    internal var scrimOverlayId = -1L

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            TextPreference("revanced_player_overlay_opacity", InputType.NUMBER)
        )

        scrimOverlayId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "scrim_overlay"
        }.id
    }
}
