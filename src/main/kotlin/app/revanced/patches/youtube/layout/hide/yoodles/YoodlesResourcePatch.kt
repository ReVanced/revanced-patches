package app.revanced.patches.youtube.layout.hide.yoodles

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

internal object YoodlesResourcePatch : ResourcePatch() {

    var youTubeLogo = -1L

    override fun execute(context: ResourceContext) {
        youTubeLogo = ResourceMappingPatch[
            "id",
            "youtube_logo"
        ]
    }
}