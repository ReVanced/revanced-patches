package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

val disablePiracyDetectionPatch = bytecodePatch {

    execute {
        piracyDetectionFingerprint.matchOrThrow.method.addInstruction(0, "return-void")
    }
}
