package app.revanced.patches.soundcloud.analytics

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Disables SoundCloud's telemetry system.",
) {
    compatibleWith("com.soundcloud.android")

    val createTrackingApiMatch by createTrackingApiFingerprint()

    execute {
        // Empty the "backend" argument to abort the initializer.
        createTrackingApiMatch.mutableMethod.addInstruction(0, "const-string p1, \"\"")
    }
}
