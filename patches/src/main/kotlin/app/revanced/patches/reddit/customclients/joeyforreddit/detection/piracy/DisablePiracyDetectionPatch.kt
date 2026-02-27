package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disablePiracyDetectionPatch = bytecodePatch {
    apply {
        detectPiracyMethod.returnEarly()
    }
}
