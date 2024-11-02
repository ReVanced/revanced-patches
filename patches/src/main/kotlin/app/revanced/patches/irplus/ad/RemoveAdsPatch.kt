package app.revanced.patches.irplus.ad

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
) {
    compatibleWith("net.binarymode.android.irplus")

    execute {
        // By overwriting the second parameter of the method,
        // the view which holds the advertisement is removed.
        irplusAdsFingerprint.matchOrThrow.method.addInstruction(0, "const/4 p2, 0x0")
    }
}
