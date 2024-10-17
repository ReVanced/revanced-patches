package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

@Patch(
    dependencies = [ResourceMappingPatch::class],
)
internal object ChangeLinkSharingDomainResourcePatch : ResourcePatch() {
    internal var tweetShareLinkTemplateId: Long = -1

    override fun execute(context: ResourceContext) {
        tweetShareLinkTemplateId = ResourceMappingPatch["string", "tweet_share_link"]
    }
}
