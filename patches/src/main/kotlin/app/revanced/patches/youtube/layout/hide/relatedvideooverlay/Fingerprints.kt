package app.revanced.patches.youtube.layout.hide.relatedvideooverlay

import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val relatedEndScreenResultsFingerprint = fingerprint {
    returns("V")
    literal{ appRelatedEndScreenResults }
}
