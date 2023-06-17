package app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.syncforreddit.detection.piracy.fingerprints.PiracyDetectionFingerprint

@Description("Disables detection of modified versions.")
@Version("0.0.1")
class DisablePiracyDetectionPatch : BytecodePatch(listOf(PiracyDetectionFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        PiracyDetectionFingerprint.result?.mutableMethod?.apply {
            addInstruction(
                0,
                """
                    return-void
                """
            )
        } ?: return PiracyDetectionFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}