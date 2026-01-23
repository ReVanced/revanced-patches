package app.revanced.patches.facebook.ads.story

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.mutable.MutableMethod

@Suppress("unused")
val `Hide story ads` by creatingBytecodePatch(
    description = "Hides the ads in the Facebook app stories.",
) {
    compatibleWith("com.facebook.katana")

    apply {
        setOf(fetchMoreAdsMethod, adsInsertionMethod).forEach(MutableMethod::returnEarly)
    }
}
