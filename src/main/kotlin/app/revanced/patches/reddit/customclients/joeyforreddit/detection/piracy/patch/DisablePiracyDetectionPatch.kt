package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.fingerprints.PiracyDetectionFingerprint

class DisablePiracyDetectionPatch : BytecodePatch(listOf(PiracyDetectionFingerprint)) {
    override fun execute(context: BytecodeContext) {
        PiracyDetectionFingerprint.result?.mutableMethod?.addInstruction(
            0,
            """
                return-void
            """
        ) ?: throw PiracyDetectionFingerprint.exception
    }
}