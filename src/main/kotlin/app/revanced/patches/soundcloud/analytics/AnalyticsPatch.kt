package app.revanced.patches.soundcloud.analytics

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.soundcloud.analytics.fingerprints.TrackingFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable telemetry",
    description = "Disables SoundCloud's telemetry system.",
    compatiblePackages = [CompatiblePackage("com.soundcloud.android")]
)
@Suppress("unused")
object AnalyticsPatch : BytecodePatch(
    setOf(TrackingFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Empties the "backend" argument to abort the initializer
        TrackingFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                const-string p1, ""
            """
        )

    }
}
