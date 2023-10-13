package app.revanced.patches.twitter.misc.links

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.links.fingerprints.AddTelemetryToLinkFingerprint

@Patch(
    name = "Remove link telemetry",
    description = "Removes telemetry at the end of the links",
    compatiblePackages = [CompatiblePackage("com.twitter.android")]
)
@Suppress("unused")
object RemoveLinkTelemetryPatch : BytecodePatch(
    setOf(AddTelemetryToLinkFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Remove telemetry from links
        AddTelemetryToLinkFingerprint.result?.mutableMethod?.addInstruction(0, "return-object p0")
            ?: throw AddTelemetryToLinkFingerprint.exception
    }
}
