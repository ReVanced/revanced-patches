package app.revanced.patches.youtube.misc.minimizedplayback

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(
    dependencies = [ResourceMappingPatch::class],
)
internal object MinimizedPlaybackResourcePatch : ResourcePatch() {
    internal var prefBackgroundAndOfflineCategoryId: Long = -1

    override fun execute(context: ResourceContext) {
        prefBackgroundAndOfflineCategoryId = ResourceMappingPatch["string", "pref_background_and_offline_category"]
    }
}
