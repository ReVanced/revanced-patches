package app.revanced.patches.irplus.ad

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Remove ads` by creatingBytecodePatch {
    compatibleWith("net.binarymode.android.irplus")

    apply {
        // By overwriting the second parameter of the method,
        // the view which holds the advertisement is removed.
        irplusAdsMethod.addInstruction(0, "const/4 p2, 0x0")
    }
}
