package app.revanced.patches.threads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.meta.ads.adInjectorFingerprint
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("com.instagram.barcelona"("382.0.0.51.85"))

    execute {
        adInjectorFingerprint.method.returnEarly(false)
    }
}
