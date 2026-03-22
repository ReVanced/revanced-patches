package app.revanced.patches.instagram.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.meta.ads.adInjectorMethod
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch("Hide ads") {
    compatibleWith("com.instagram.android"("421.0.0.51.66"))

    apply {
        adInjectorMethod.returnEarly(false)
    }
}
