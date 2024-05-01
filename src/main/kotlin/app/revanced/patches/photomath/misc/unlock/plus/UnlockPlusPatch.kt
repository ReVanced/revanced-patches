package app.revanced.patches.photomath.misc.unlock.plus

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.patches.photomath.misc.unlock.bookpoint.enableBookpointPatch
import app.revanced.patches.photomath.misc.unlock.plus.fingerprints.isPlusUnlockedFingerprint

@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock plus",
    description = "Unlocks plus features.",
){
    compatibleWith("com.microblink.photomath"("8.32.0"))

    dependsOn(
        signatureDetectionPatch,
        enableBookpointPatch
    )

    val isPlusUnlockedResult by isPlusUnlockedFingerprint
    isPlusUnlockedResult.mutableMethod.addInstructions(
        0,
        """
            const/4 v0, 0x1
            return v0
        """
    )
}