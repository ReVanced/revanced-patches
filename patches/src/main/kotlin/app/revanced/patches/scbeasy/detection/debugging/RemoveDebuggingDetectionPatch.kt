package app.revanced.patches.scbeasy.detection.debugging

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val debuggingDetectionPatches = bytecodePatch(
    name = "Remove debugging detection",
    description = "Removes the USB and wireless debugging checks.",
    use = false,
) {
    compatibleWith("com.scb.phone")

    val debuggingDetectionMatch by debuggingDetectionFingerprint()

    execute {
        debuggingDetectionMatch.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
