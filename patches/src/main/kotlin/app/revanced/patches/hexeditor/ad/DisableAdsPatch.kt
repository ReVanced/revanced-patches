package app.revanced.patches.hexeditor.ad

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Disable ads` by creatingBytecodePatch {
    compatibleWith("com.myprog.hexedit")

    apply {
        primaryAdsMethod.returnEarly(true)
    }
}
