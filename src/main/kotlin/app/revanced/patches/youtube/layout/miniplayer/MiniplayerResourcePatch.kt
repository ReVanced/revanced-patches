package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings

var floatyBarButtonTopMargin = -1L
    private set

// Only available in 19.15 and upwards.
var ytOutlineXWhite24 = -1L
    private set
var ytOutlinePictureInPictureWhite24 = -1L
    private set
var scrimOverlay = -1L
    private set
var modernMiniplayerClose = -1L
    private set
var modernMiniplayerExpand = -1L
    private set
var modernMiniplayerRewindButton = -1L
    private set
var modernMiniplayerForwardButton = -1L
    private set
var playerOverlays = -1L
    private set

internal val miniplayerResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        floatyBarButtonTopMargin = resourceMappings[
            "dimen",
            "floaty_bar_button_top_margin",
        ]

        try {
            ytOutlinePictureInPictureWhite24 = resourceMappings[
                "drawable",
                "yt_outline_picture_in_picture_white_24",
            ]
        } catch (exception: PatchException) {
            // Ignore, and assume the app is 19.14 or earlier.
            return@execute
        }

        ytOutlineXWhite24 = resourceMappings[
            "drawable",
            "yt_outline_x_white_24",
        ]

        scrimOverlay = resourceMappings[
            "id",
            "scrim_overlay",
        ]

        modernMiniplayerClose = resourceMappings[
            "id",
            "modern_miniplayer_close",
        ]

        modernMiniplayerExpand = resourceMappings[
            "id",
            "modern_miniplayer_expand",
        ]

        modernMiniplayerRewindButton = resourceMappings[
            "id",
            "modern_miniplayer_rewind_button",
        ]

        modernMiniplayerForwardButton = resourceMappings[
            "id",
            "modern_miniplayer_forward_button",
        ]

        playerOverlays = resourceMappings[
            "layout",
            "player_overlays",
        ]

        // Resource id is not used during patching, but is used by integrations.
        // Verify the resource is present while patching.
        resourceMappings[
            "id",
            "modern_miniplayer_subtitle_text",
        ]
    }
}
