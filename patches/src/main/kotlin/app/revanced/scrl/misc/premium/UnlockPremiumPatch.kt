package app.revanced.patches.scrl.misc.premium

import app.revanced.util.returnEarly
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
) {
    compatibleWith("com.appostrophe.scrl")

    execute {
        isPremiumVersionFingerprint.method.returnEarly(true)
    }
}