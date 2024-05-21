package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings

internal var imageOnlyTabResourceId = -1L
internal var actionBarSearchResultsViewMicId = -1L

internal val navigationBarHookResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        imageOnlyTabResourceId = resourceMappings["layout", "image_only_tab"]
        actionBarSearchResultsViewMicId = resourceMappings["layout", "action_bar_search_results_view_mic"]
    }
}
