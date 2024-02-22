package app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.fingerprints.PiracyDetectionFingerprint

@Patch(description = "Disables detection of modified versions.")
object DisablePiracyDetectionPatch : BytecodePatch(setOf(PiracyDetectionFingerprint)) {
    override fun execute(context: BytecodeContext) {
        // Do not throw an error if the fingerprint is not resolved.
        // This is fine because new versions of the target app do not need this patch.
        PiracyDetectionFingerprint.result?.mutableMethod?.apply {
            addInstruction(0, "return-void")
        }
    }
}
