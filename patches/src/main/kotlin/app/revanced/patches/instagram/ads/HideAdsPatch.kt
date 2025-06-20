package app.revanced.patches.instagram.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.meta.ads.adInjectorFingerprint
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("com.instagram.android")

    execute {
        adInjectorFingerprint.method.returnEarly(false)
    }
}
