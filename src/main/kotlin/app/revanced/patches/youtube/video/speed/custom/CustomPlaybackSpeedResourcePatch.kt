package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch

internal object CustomPlaybackSpeedResourcePatch : ResourcePatch() {
    var speedUnavailableId: Long = -1

    override fun execute(context: ResourceContext) {
        speedUnavailableId = ResourceMappingPatch.resourceMappings.single {
            it.type == "string" && it.name == "varispeed_unavailable_message"
        }.id
    }
}