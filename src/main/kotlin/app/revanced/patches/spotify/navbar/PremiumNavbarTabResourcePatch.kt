package app.revanced.patches.spotify.navbar

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings

internal var showBottomNavigationItemsTextId = -1L
    private set
internal var premiumTabId = -1L
    private set

@Suppress("unused")
val premiumNavbarTabResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute { context ->
        premiumTabId = resourceMappings["id", "premium_tab"]

        showBottomNavigationItemsTextId = resourceMappings[
            "bool",
            "show_bottom_navigation_items_text",
        ]
    }
}
