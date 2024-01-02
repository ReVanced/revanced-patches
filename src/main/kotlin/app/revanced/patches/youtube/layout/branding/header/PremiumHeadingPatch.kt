package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch

@Deprecated("Use PremiumHeadingPatch instead.")
object PremiumHeadingPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) = CustomHeaderPatch.execute(context)
}
