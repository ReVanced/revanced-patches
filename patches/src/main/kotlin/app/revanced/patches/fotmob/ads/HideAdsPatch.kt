package app.revanced.patches.fotmob.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("com.mobilefootie.wc2010")

    execute {
        shouldDisplayAdsMethod.method.returnEarly(false)
    }
}