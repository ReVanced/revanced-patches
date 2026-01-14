package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disablePiracyDetectionPatch = bytecodePatch {
    apply {
        piracyDetectionMethod.addInstruction(0, "return-void")
    }
}
