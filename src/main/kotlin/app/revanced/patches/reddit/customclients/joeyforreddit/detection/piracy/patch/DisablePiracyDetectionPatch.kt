package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.fingerprints.PiracyDetectionFingerprint

class DisablePiracyDetectionPatch : BytecodePatch(listOf(PiracyDetectionFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        PiracyDetectionFingerprint.result?.mutableMethod?.addInstruction(
            0,
            """
                return-void
            """
        ) ?: return PiracyDetectionFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}