package app.revanced.patches.irplus.ad

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.irplus.ad.fingerprints.irplusAdsFingerprint

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads"
) {
    compatibleWith("net.binarymode.android.irplus")

    val irplusAdsResult by irplusAdsFingerprint

    execute {
        // By overwriting the second parameter of the method,
        // the view which holds the advertisement is removed.
        irplusAdsResult.mutableMethod.addInstruction(0, "const/4 p2, 0x0")
    }
}