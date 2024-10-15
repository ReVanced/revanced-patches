package app.revanced.patches.youtube.layout.buttons.player.hide

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(dependencies = [ResourceMappingPatch::class])
internal object HidePlayerButtonsResourcePatch : ResourcePatch() {
    var playerControlPreviousButton = -1L
    var playerControlNextButton = -1L

    override fun execute(context: ResourceContext) {
        playerControlPreviousButton = ResourceMappingPatch[
            "id",
            "player_control_previous_button"
        ]

        playerControlNextButton = ResourceMappingPatch[
            "id",
            "player_control_next_button"
        ]
    }
}
