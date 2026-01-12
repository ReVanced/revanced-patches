package app.revanced.patches.reddit.customclients.sync.detection.piracy

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

val `Disable piracy detection` by creatingBytecodePatch(
    description = "Disables detection of modified versions.",
) {

    apply {
        // Do not throw an error if the fingerprint is not resolved.
        // This is fine because new versions of the target app do not need this patch.
        piracyDetectionMethod.addInstruction(0, "return-void")
    }
}
