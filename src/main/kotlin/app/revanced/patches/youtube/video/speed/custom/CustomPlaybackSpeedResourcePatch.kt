package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch

internal object CustomPlaybackSpeedResourcePatch : ResourcePatch() {
    var speedUnavailableId: Long = -1

    override fun execute(context: ResourceContext) {
        speedUnavailableId = ResourceMappingPatch[
            "string",
            "varispeed_unavailable_message",
        ]
    }
}
