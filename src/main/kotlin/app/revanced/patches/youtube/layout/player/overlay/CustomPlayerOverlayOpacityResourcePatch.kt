package app.revanced.patches.youtube.layout.player.overlay

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [SettingsPatch::class, ResourceMappingPatch::class]
)
object CustomPlayerOverlayOpacityResourcePatch : ResourcePatch() {
    internal var scrimOverlayId = -1L

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            TextPreference(
                "revanced_player_overlay_opacity",
                StringResource(
                    "revanced_player_overlay_opacity_title",
                    "Player overlay opacity"
                ),
                StringResource(
                    "revanced_player_overlay_opacity_summary",
                    "Opacity value between 0-100, where 0 is transparent"
                ),
                InputType.NUMBER
            )
        )

        scrimOverlayId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "scrim_overlay"
        }.id
    }
}
