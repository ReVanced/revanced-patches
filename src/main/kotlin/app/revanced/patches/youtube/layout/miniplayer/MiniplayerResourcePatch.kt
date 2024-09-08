package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.util.ResourceUtils

@Patch(dependencies = [ResourceMappingPatch::class])
internal object MiniplayerResourcePatch : ResourcePatch() {
    var floatyBarButtonTopMargin = -1L

    // Only available in 19.15 and upwards.
    var ytOutlineXWhite24 = -1L
    var ytOutlinePictureInPictureWhite24 = -1L
    var scrimOverlay = -1L
    var modernMiniplayerClose = -1L
    var modernMiniplayerExpand = -1L
    var modernMiniplayerRewindButton = -1L
    var modernMiniplayerForwardButton = -1L
    var playerOverlays = -1L

    // TODO: Extract these version checks into a shared resource patch.

    var is_19_15_or_greater = false
    var is_19_16_or_greater = false
    var is_19_17_or_greater = false
    var is_19_19_or_greater = false
    var is_19_23_or_greater = false
    var is_19_24_or_greater = false
    var is_19_25_or_greater = false
    var is_19_26_or_greater = false

    override fun execute(context: ResourceContext) {
        val playVersion = ResourceUtils.getPlayServicesVersion(context)

        is_19_15_or_greater = 241602000 <= playVersion
        is_19_16_or_greater = 241702000 <= playVersion
        is_19_17_or_greater = 241802000 <= playVersion
        is_19_19_or_greater = 241999000 <= playVersion
        is_19_24_or_greater = 242505000 <= playVersion
        is_19_23_or_greater = 242402000 <= playVersion
        is_19_25_or_greater = 242599000 <= playVersion
        is_19_26_or_greater = 242705000 <= playVersion

        floatyBarButtonTopMargin = ResourceMappingPatch[
            "dimen",
            "floaty_bar_button_top_margin"
        ]

        // Only required for 19.16
        if (is_19_16_or_greater && !is_19_17_or_greater) {
            ytOutlinePictureInPictureWhite24 = ResourceMappingPatch[
                "drawable",
                "yt_outline_picture_in_picture_white_24"
            ]
        }

        ytOutlineXWhite24 = ResourceMappingPatch[
            "drawable",
            "yt_outline_x_white_24"
        ]

        scrimOverlay = ResourceMappingPatch[
            "id",
            "scrim_overlay"
        ]

        try {
            modernMiniplayerClose = ResourceMappingPatch[
                "id",
                "modern_miniplayer_close"
            ]
        } catch (exception: PatchException) {
            // Ignore, and assume the app is 19.14 or earlier.
            return
        }

        modernMiniplayerExpand = ResourceMappingPatch[
            "id",
            "modern_miniplayer_expand"
        ]

        modernMiniplayerRewindButton = ResourceMappingPatch[
            "id",
            "modern_miniplayer_rewind_button"
        ]

        modernMiniplayerForwardButton = ResourceMappingPatch[
            "id",
            "modern_miniplayer_forward_button"
        ]

        playerOverlays = ResourceMappingPatch[
            "layout",
            "player_overlays"
        ]

        // Resource id is not used during patching, but is used by integrations.
        // Verify the resource is present while patching.
        ResourceMappingPatch[
            "id",
            "modern_miniplayer_subtitle_text"
        ]
    }
}
