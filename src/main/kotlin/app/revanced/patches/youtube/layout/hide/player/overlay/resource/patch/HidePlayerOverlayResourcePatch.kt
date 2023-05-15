package app.revanced.patches.youtube.layout.hide.player.overlay.resource.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.hide.player.overlay.annotations.HidePlayerOverlayPatchCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import jdk.jfr.Name

@Name("hide-player-overlay-resource-patch")
@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
@HidePlayerOverlayPatchCompatibility
class HidePlayerOverlayResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_player_overlay",
                StringResource("revanced_hide_player_overlay_title", "Hide background overlay in player"),
                StringResource("revanced_hide_player_overlay_summary_on", "Background overlay is hidden"),
                StringResource("revanced_hide_player_overlay_summary_off", "Background overlay is shown")
            )
        )

        scrimOverlayId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "scrim_overlay"
        }.id

        return PatchResultSuccess()
    }

    internal companion object {
        var scrimOverlayId: Long = -1
    }
}