package app.revanced.patches.photomath.misc.unlockplus.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.photomath.detection.signature.patch.SignatureDetectionPatch
import app.revanced.patches.photomath.misc.unlockplus.annotations.UnlockPlusCompatibilty
import app.revanced.patches.photomath.misc.unlockplus.fingerprints.IsPlusUnlockedFingerprint

@Patch
@Name("Unlock plus")
@DependsOn([SignatureDetectionPatch::class])
@Description("Unlocks plus features.")
@UnlockPlusCompatibilty
class UnlockPlusPatch : BytecodePatch(
    listOf(
        IsPlusUnlockedFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        IsPlusUnlockedFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        } ?: throw IsPlusUnlockedFingerprint.exception
    }

}