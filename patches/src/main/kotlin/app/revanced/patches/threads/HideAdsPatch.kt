package app.revanced.patches.threads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.meta.ads.adInjectorMethod
import app.revanced.util.returnEarly

@Suppress("unused")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("com.instagram.barcelona"("382.0.0.51.85"))

    apply {
        adInjectorMethod.returnEarly(false)
    }
}
