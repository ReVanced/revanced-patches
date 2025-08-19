package app.revanced.patches.pixiv.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("jp.pxv.android"("6.141.1"))

    execute {
        shouldShowAdsFingerprint.method.returnEarly(false)
    }
}
