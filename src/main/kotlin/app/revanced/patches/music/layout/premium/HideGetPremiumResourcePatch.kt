package app.revanced.patches.music.layout.premium

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(dependencies = [ResourceMappingPatch::class])
object HideGetPremiumResourcePatch : ResourcePatch() {
    internal var privacyTosFooterId = -1L

    override fun execute(context: ResourceContext) {
        privacyTosFooterId = ResourceMappingPatch.resourceMappings.first {
            it.type == "id" && it.name == "privacy_tos_footer"
        }.id
    }
}
