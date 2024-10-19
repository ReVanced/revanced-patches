package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch

@Patch(dependencies = [ResourceMappingPatch::class, VersionCheckPatch::class])
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
    var miniplayerMaxSize = -1L

    override fun execute(context: ResourceContext) {
        floatyBarButtonTopMargin = ResourceMappingPatch[
            "dimen",
            "floaty_bar_button_top_margin"
        ]

        scrimOverlay = ResourceMappingPatch[
            "id",
            "scrim_overlay"
        ]

        playerOverlays = ResourceMappingPatch[
            "layout",
            "player_overlays"
        ]

        if (VersionCheckPatch.is_19_16_or_greater) {
            modernMiniplayerClose = ResourceMappingPatch[
                "id",
                "modern_miniplayer_close"
            ]

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

            // Resource id is not used during patching, but is used by integrations.
            // Verify the resource is present while patching.
            ResourceMappingPatch[
                "id",
                "modern_miniplayer_subtitle_text"
            ]

            // Only required for exactly 19.16
            if (!VersionCheckPatch.is_19_17_or_greater) {
                ytOutlinePictureInPictureWhite24 = ResourceMappingPatch[
                    "drawable",
                    "yt_outline_picture_in_picture_white_24"
                ]

                ytOutlineXWhite24 = ResourceMappingPatch[
                    "drawable",
                    "yt_outline_x_white_24"
                ]
            }

            if (VersionCheckPatch.is_19_26_or_greater) {
                miniplayerMaxSize = ResourceMappingPatch[
                    "dimen",
                    "miniplayer_max_size"
                ]
            }
        }
    }
}
