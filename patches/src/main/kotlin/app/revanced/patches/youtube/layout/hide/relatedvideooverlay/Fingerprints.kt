package app.revanced.patches.youtube.layout.hide.relatedvideooverlay

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral

internal val relatedEndScreenResultsParentFingerprint by fingerprint {
    returns("V")
    instructions(
        resourceLiteral(ResourceType.LAYOUT, "app_related_endscreen_results")
    )
}

internal val relatedEndScreenResultsFingerprint by fingerprint {
    returns("V")
    parameters(
        "I",
        "Z",
        "I",
    )
}
