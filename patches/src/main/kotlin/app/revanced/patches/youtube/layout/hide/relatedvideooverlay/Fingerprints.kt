package app.revanced.patches.youtube.layout.hide.relatedvideooverlay

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceType

internal val relatedEndScreenResultsParentFingerprint = fingerprint {
    returns("V")
    instructions(
        ResourceType.LAYOUT("app_related_endscreen_results"),
    )
}

internal val relatedEndScreenResultsFingerprint = fingerprint {
    returns("V")
    parameters(
        "I",
        "Z",
        "I",
    )
}
