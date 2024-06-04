package app.revanced.patches.serviceportalbund.detection.root

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.serviceportalbund.detection.root.fingerprints.rootDetectionFingerprint

@Suppress("unused")
val rootDetectionPatch = bytecodePatch(
    name = "Remove root detection",
    description = "Removes the check for root permissions and unlocked bootloader.",
) {
    compatibleWith("at.gv.bka.serviceportal")

    val rootDetectionResult by rootDetectionFingerprint

    execute {
        rootDetectionResult.mutableMethod.addInstruction(0, "return-void")
    }
}