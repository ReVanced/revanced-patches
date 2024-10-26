package app.revanced.patches.serviceportalbund.detection.root

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val rootDetectionPatch = bytecodePatch(
    name = "Remove root detection",
    description = "Removes the check for root permissions and unlocked bootloader.",
) {
    compatibleWith("at.gv.bka.serviceportal")

    val rootDetectionMatch by rootDetectionFingerprint()

    execute {
        rootDetectionMatch.mutableMethod.addInstruction(0, "return-void")
    }
}
