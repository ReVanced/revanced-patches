package app.revanced.patches.soundcloud.analytics

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.soundcloud.analytics.fingerprints.CreateTrackingApiFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable telemetry",
    description = "Disables SoundCloud's telemetry system.",
    compatiblePackages = [CompatiblePackage("com.soundcloud.android")],
)
@Suppress("unused")
object DisableTelemetryPatch : BytecodePatch(
    setOf(CreateTrackingApiFingerprint),
) {
    override fun execute(context: BytecodeContext) =
        // Empty the "backend" argument to abort the initializer.
        CreateTrackingApiFingerprint.resultOrThrow()
            .mutableMethod.addInstruction(0, "const-string p1, \"\"")
}
