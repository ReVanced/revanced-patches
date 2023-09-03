package app.revanced.patches.photomath.misc.unlockplus.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.photomath.detection.signature.patch.SignatureDetectionPatch
import app.revanced.patches.photomath.misc.bookpoint.patch.EnableBookpointPatch
import app.revanced.patches.photomath.misc.unlockplus.fingerprints.IsPlusUnlockedFingerprint

@Patch
@Name("Unlock plus")
@DependsOn([SignatureDetectionPatch::class, EnableBookpointPatch::class])
@Compatibility([Package("com.microblink.photomath")])
class UnlockPlusPatch : BytecodePatch(
    listOf(IsPlusUnlockedFingerprint)
) {
    override fun execute(context: BytecodeContext) = IsPlusUnlockedFingerprint.result?.mutableMethod?.addInstructions(
        0,
        """
            const/4 v0, 0x1
            return v0
        """
    ) ?: throw IsPlusUnlockedFingerprint.exception
}