package app.revanced.patches.youtube.layout.hide.relatedvideooverlay

import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val relatedEndScreenResultsParentFingerprint = fingerprint {
    returns("V")
    literal{ appRelatedEndScreenResults }
}

internal val relatedEndScreenResultsFingerprint = fingerprint {
    returns("V")
    parameters(
        "I",
        "Z",
        "I",
    )
}
