package app.revanced.patches.soundcloud.analytics

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Disable telemetry` by creatingBytecodePatch(
    description = "Disables SoundCloud's telemetry system.",
) {
    compatibleWith("com.soundcloud.android"("2025.05.27-release"))

    apply {
        // Empty the "backend" argument to abort the initializer.
        createTrackingApiMethod.addInstruction(0, "const-string p1, \"\"")
    }
}
