package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

val disablePiracyDetectionPatch = bytecodePatch {

    execute {
        piracyDetectionFingerprint.method.addInstruction(0, "return-void")
    }
}
