package app.revanced.patches.yuka.misc.unlockpremium.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.yuka.misc.unlockpremium.annotations.UnlockPremiumCompatibility
import app.revanced.patches.yuka.misc.unlockpremium.fingerprints.IsPremiumFingerprint
import app.revanced.patches.yuka.misc.unlockpremium.fingerprints.YukaUserConstructorFingerprint

@Patch
@Name("unlock-premium")
@Description("Unlocks premium features.")
@UnlockPremiumCompatibility
@Version("0.0.1")
class UnlockPremiunPatch : BytecodePatch(
    listOf(
        YukaUserConstructorFingerprint
    )
) {

    override fun execute(context: BytecodeContext): PatchResult {
        IsPremiumFingerprint.resolve(context,YukaUserConstructorFingerprint.result!!.classDef)
        val method = IsPremiumFingerprint.result!!.mutableMethod
        method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
        return PatchResultSuccess()
    }

}