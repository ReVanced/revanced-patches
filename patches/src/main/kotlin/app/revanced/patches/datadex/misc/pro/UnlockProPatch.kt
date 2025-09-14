package app.revanced.patches.datadex.misc.pro

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock PRO",
) {
    compatibleWith("com.talzz.datadex"("3.25"))

    execute {
        // force isPro() to always return true
        isProFingerprint.method.returnEarly(true)
    }
}
