package app.revanced.patches.reddit.customclients.sync.detection.piracy

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disablePiracyDetectionPatch = bytecodePatch(
    description = "Disables detection of modified versions.",
) {
    val piracyDetectionMatch by piracyDetectionFingerprint()

    execute {
        // Do not throw an error if the fingerprint is not resolved.
        // This is fine because new versions of the target app do not need this patch.
        piracyDetectionMatch.mutableMethod.addInstruction(0, "return-void")
    }
}
