package app.revanced.patches.scbeasy.detection.debugging

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.scbeasy.detection.debugging.fingerprints.debuggingDetectionFingerprint

@Suppress("unused")
val RemoveDebuggingDetectionPatch = bytecodePatch(
    name = "Remove debugging detection",
    description = "Removes the USB and wireless debugging checks.",
) {
    compatibleWith("com.scb.phone"())

    val debuggingDetectionResult by debuggingDetectionFingerprint

    execute {
        debuggingDetectionResult.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )
    }
}
