package app.revanced.patches.irplus.ad

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.irplus.ad.fingerprints.IrplusAdsFingerprint


@Patch(
    name = "Remove ads",
    compatiblePackages = [CompatiblePackage("net.binarymode.android.irplus")]
)
@Suppress("unused")
object RemoveAdsPatch : BytecodePatch(
    setOf(IrplusAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val method = IrplusAdsFingerprint.result!!.mutableMethod

        // By overwriting the second parameter of the method,
        // the view which holds the advertisement is removed.
        method.addInstruction(0, "const/4 p2, 0x0")
    }
}