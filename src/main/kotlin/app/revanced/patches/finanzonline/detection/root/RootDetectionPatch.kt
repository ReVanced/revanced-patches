package app.revanced.patches.finanzonline.detection.root

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.finanzonline.detection.root.fingerprints.rootDetectionFingerprint

@Suppress("unused")
val rootDetectionPatch = bytecodePatch(
    name = "Root detection",
    description = "Removes the check for root permissions.",
) {
    compatibleWith("at.gv.bmf.bmf2go"())

    val rootDetectionResult by rootDetectionFingerprint

    execute {
        rootDetectionResult.mutableMethod.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """
        )
    }
}
