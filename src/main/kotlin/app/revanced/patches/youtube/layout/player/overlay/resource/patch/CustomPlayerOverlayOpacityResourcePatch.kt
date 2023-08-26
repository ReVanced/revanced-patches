package app.revanced.patches.youtube.layout.player.overlay.resource.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
class CustomPlayerOverlayOpacityResourcePatch : ResourcePatch {
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

    internal companion object {
        var scrimOverlayId = -1L
    }
}
