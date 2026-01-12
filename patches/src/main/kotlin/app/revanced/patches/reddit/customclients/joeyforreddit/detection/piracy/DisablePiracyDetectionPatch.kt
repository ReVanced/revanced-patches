package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Disable piracy detection` by creatingBytecodePatch {
    apply {
        piracyDetectionMethod.addInstruction(0, "return-void")
    }
}
