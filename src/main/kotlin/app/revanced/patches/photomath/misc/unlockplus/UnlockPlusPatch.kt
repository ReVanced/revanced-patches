package app.revanced.patches.photomath.misc.unlockplus

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.photomath.detection.signature.SignatureDetectionPatch
import app.revanced.patches.photomath.misc.bookpoint.EnableBookpointPatch
import app.revanced.patches.photomath.misc.unlockplus.fingerprints.IsPlusUnlockedFingerprint

@Patch(
    name = "Unlock plus",
    dependencies = [SignatureDetectionPatch::class, EnableBookpointPatch::class],
    compatiblePackages = [CompatiblePackage("com.microblink.photomath")]
)
@Suppress("unused")
object UnlockPlusPatch : BytecodePatch(
    setOf(IsPlusUnlockedFingerprint)
){
    override fun execute(context: BytecodeContext) = IsPlusUnlockedFingerprint.result?.mutableMethod?.addInstructions(
        0,
        """
            const/4 v0, 0x1
            return v0
        """
    ) ?: throw IsPlusUnlockedFingerprint.exception
}