package app.revanced.patches.instagram.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.meta.ads.adInjectorMethod
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("com.instagram.android")

    apply {
        adInjectorMethod.returnEarly(false)
    }
}
