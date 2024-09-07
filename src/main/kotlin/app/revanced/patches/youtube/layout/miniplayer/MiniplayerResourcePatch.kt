package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.util.getNode
import org.w3c.dom.Element

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

    override fun execute(context: ResourceContext) {
        val appVersionName = context.document["AndroidManifest.xml"].use { document ->
            val manifestElement = document.getNode("manifest") as Element
            manifestElement.getAttribute("android:versionName")
        }

        floatyBarButtonTopMargin = ResourceMappingPatch[
            "dimen",
            "floaty_bar_button_top_margin"
        ]

        // Only required for 19.16.
        if (appVersionName.contains("19.16")) {
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
