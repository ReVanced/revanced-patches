package app.revanced.patches.soundcloud.analytics

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Disables SoundCloud's telemetry system.",
) {
    compatibleWith("com.soundcloud.android"("2025.05.27-release"))

    execute {
        // Empty the "backend" argument to abort the initializer.
        createTrackingApiFingerprint.method.addInstruction(0, "const-string p1, \"\"")
    }
}
