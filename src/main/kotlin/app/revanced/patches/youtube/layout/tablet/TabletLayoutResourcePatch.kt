package app.revanced.patches.youtube.layout.tablet

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(dependencies = [ResourceMappingPatch::class])
internal object TabletLayoutResourcePatch : ResourcePatch() {
    var floatyBarButtonTopMargin = -1L
    var modernMiniplayerClose = -1L
    var modernMiniplayerExpand = -1L

    override fun execute(context: ResourceContext) {
        floatyBarButtonTopMargin = ResourceMappingPatch[
            "dimen",
            "floaty_bar_button_top_margin"
        ]

        modernMiniplayerClose = ResourceMappingPatch[
            "id",
            "modern_miniplayer_close"
        ]

        modernMiniplayerExpand = ResourceMappingPatch[
            "id",
            "modern_miniplayer_expand"
        ]
    }
}
