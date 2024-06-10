package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings

internal var expandButtonDownId = -1L

internal val hideLayoutComponentsResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        expandButtonDownId = resourceMappings[
            "layout",
            "expand_button_down",
        ]
    }
}
