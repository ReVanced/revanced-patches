package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.fingerprints.piracyDetectionFingerprint

@Suppress("unused")
val disablePiracyDetectionPatch = bytecodePatch {
    val piracyDetectionResult by piracyDetectionFingerprint

    execute {
        piracyDetectionResult.mutableMethod.addInstruction(
            0,
            """
                return-void
            """
        )
    }
}