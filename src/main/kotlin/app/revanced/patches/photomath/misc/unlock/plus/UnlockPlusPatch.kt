package app.revanced.patches.photomath.misc.unlock.plus

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.patches.photomath.misc.unlock.bookpoint.enableBookpointPatch
import app.revanced.patches.photomath.misc.unlock.plus.fingerprints.isPlusUnlockedFingerprint

@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock plus"
){
    dependsOn(
        signatureDetectionPatch,
        enableBookpointPatch
    )

    compatibleWith("com.microblink.photomath"("8.32.0"))

    val isPlusUnlockedResult by isPlusUnlockedFingerprint

    execute {
        isPlusUnlockedResult.mutableMethod.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
        """
        )
    }
}