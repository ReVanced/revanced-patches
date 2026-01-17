package app.revanced.patches.pixiv.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("jp.pxv.android"("6.141.1"))

    apply {
        shouldShowAdsMethod.returnEarly(false)
    }
}
