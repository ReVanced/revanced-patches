package app.revanced.patches.hexeditor.ad

import app.revanced.util.returnEarly
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Disable ads` by creatingBytecodePatch {
    compatibleWith("com.myprog.hexedit")

    apply {
        primaryAdsMethod.returnEarly(true)
    }
}
