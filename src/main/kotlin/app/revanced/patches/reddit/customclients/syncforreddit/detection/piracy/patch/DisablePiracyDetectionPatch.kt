package app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.fingerprints.PiracyDetectionFingerprint

@Description("Disables detection of modified versions.")
class DisablePiracyDetectionPatch : BytecodePatch(listOf(PiracyDetectionFingerprint)) {
    override fun execute(context: BytecodeContext) {
        // Do not return an error if the fingerprint is not resolved.
        // This is fine because new versions of the target app do not need this patch.
        PiracyDetectionFingerprint.result?.mutableMethod?.apply {
            addInstruction(
                0,
                """
                    return-void
                """
            )
        }
    }
}