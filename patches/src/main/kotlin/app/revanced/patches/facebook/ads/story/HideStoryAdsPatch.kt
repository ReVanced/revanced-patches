package app.revanced.patches.facebook.ads.story

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.mutable.MutableMethod

@Suppress("unused")
val hideStoryAdsPatch = bytecodePatch(
    name = "Hide story ads",
    description = "Hides the ads in the Facebook app stories.",
) {
    compatibleWith("com.facebook.katana")

    apply {
        setOf(fetchMoreAdsMethod, adsInsertionMethod).forEach(MutableMethod::returnEarly)
    }
}
