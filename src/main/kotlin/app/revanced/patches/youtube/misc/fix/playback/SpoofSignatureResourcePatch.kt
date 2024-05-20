package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(dependencies = [ResourceMappingPatch::class])
@Deprecated("This patch will be removed in the future.")
object SpoofSignatureResourcePatch : ResourcePatch() {
    internal var scrubbedPreviewThumbnailResourceId: Long = -1

    override fun execute(context: ResourceContext) {
        scrubbedPreviewThumbnailResourceId = ResourceMappingPatch[
            "id",
            "thumbnail",
        ]
    }
}
