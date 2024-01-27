package app.revanced.patches.spotify.navbar

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(dependencies = [ResourceMappingPatch::class])
object PremiumNavbarTabResourcePatch : ResourcePatch() {
    internal var showBottomNavigationItemsTextId = -1L
    internal var premiumTabId = -1L

    override fun execute(context: ResourceContext) {
        premiumTabId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "premium_tab"
        }.id

        showBottomNavigationItemsTextId = ResourceMappingPatch.resourceMappings.single {
            it.type == "bool" && it.name == "show_bottom_navigation_items_text"
        }.id
    }
}
