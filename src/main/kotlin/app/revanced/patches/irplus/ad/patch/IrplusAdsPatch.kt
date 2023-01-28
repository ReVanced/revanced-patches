package app.revanced.patches.irplus.ad.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.irplus.ad.annotations.IrplusAdsCompatibility
import app.revanced.patches.irplus.ad.fingerprints.IrplusAdsFingerprint


@Patch
@Name("remove-ads")
@Description("Removes all ads from the app.")
@IrplusAdsCompatibility
@Version("0.0.1")
class IrplusAdsPatch : BytecodePatch(
    listOf(IrplusAdsFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val method = IrplusAdsFingerprint.result!!.mutableMethod

        // By overwriting the second parameter of the method,
        // the view which holds the advertisement is removed.
        method.addInstruction(0, "const/4 p2, 0x0")

        return PatchResultSuccess()
    }
}