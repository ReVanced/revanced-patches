package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings

var speedUnavailableId = -1L
    internal set

internal val customPlaybackSpeedResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        speedUnavailableId = resourceMappings[
            "string",
            "varispeed_unavailable_message",
        ]
    }
}
