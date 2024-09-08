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

    // These version checks assume no new bug fix versions will be released (such as 19.16.40).
    /**
     * Supports only legacy miniplayer.
     */
    var is_19_15_36_or_less = false
    /**
     * First supported version with modern miniplayers
     */
    var is_19_16_35_or_less = false
    /**
     * Last version with Modern 1swipe to expand/close functionality.
     */
    var is_19_19_39_or_less = false
    /**
     * Last version with Modern 1 skip forward/back buttons.
     */
    var is_19_24_45_or_less = false

    override fun execute(context: ResourceContext) {
        val playVersion = ResourceUtils.getPlayServicesVersion(context)

        is_19_15_36_or_less = playVersion <= 241602000
        is_19_16_35_or_less = playVersion <= 241702000
        is_19_19_39_or_less = playVersion <= 241999000
        is_19_24_45_or_less = playVersion <= 242505000

        floatyBarButtonTopMargin = ResourceMappingPatch[
            "dimen",
            "floaty_bar_button_top_margin"
        ]

        // Only required for 19.16
        if (!is_19_15_36_or_less && is_19_16_35_or_less) {
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
