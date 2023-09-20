package app.revanced.patches.yuka.misc.unlockpremium

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.yuka.misc.unlockpremium.fingerprints.IsPremiumFingerprint
import app.revanced.patches.yuka.misc.unlockpremium.fingerprints.YukaUserConstructorFingerprint

@Patch(
    name = "Unlock premium",
    compatiblePackages = [CompatiblePackage("io.yuka.android")]
)
@Suppress("unused")
object UnlockPremiumPatch : BytecodePatch(
    setOf(YukaUserConstructorFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        IsPremiumFingerprint.resolve(context,YukaUserConstructorFingerprint.result!!.classDef)
        val method = IsPremiumFingerprint.result!!.mutableMethod
        method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}