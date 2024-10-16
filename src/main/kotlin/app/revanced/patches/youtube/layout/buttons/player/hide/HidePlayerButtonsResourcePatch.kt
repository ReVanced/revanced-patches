package app.revanced.patches.youtube.layout.buttons.player.hide

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(dependencies = [ResourceMappingPatch::class])
internal object HidePlayerButtonsResourcePatch : ResourcePatch() {
    var playerControlPreviousButtonTouchArea = -1L
    var playerControlNextButtonTouchArea = -1L

    override fun execute(context: ResourceContext) {
        playerControlPreviousButtonTouchArea = ResourceMappingPatch[
            "id",
            "player_control_previous_button_touch_area"
        ]

        playerControlNextButtonTouchArea = ResourceMappingPatch[
            "id",
            "player_control_next_button_touch_area"
        ]
    }
}
