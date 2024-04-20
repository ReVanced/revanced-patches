package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(
    dependencies = [ResourceMappingPatch::class],
)
internal object NavigationBarHookResourcePatch : ResourcePatch() {
    internal var imageOnlyTabResourceId: Long = -1
    internal var actionBarSearchResultsViewMicId: Long = -1

    override fun execute(context: ResourceContext) {
        imageOnlyTabResourceId = ResourceMappingPatch["layout", "image_only_tab"]
        actionBarSearchResultsViewMicId = ResourceMappingPatch["layout", "action_bar_search_results_view_mic"]
    }
}
