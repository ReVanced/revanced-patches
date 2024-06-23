package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disablePiracyDetectionPatch = bytecodePatch {
    val piracyDetectionMatch by piracyDetectionFingerprint()

    execute {
        piracyDetectionMatch.mutableMethod.addInstruction(0, "return-void")
    }
}
