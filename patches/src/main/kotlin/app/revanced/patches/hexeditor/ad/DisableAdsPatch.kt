package app.revanced.patches.hexeditor.ad

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableAdsPatch = bytecodePatch("Disable ads") {
    compatibleWith("com.myprog.hexedit")

    apply {
        primaryAdsMethod.returnEarly(true)
    }
}
