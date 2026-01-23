package app.revanced.patches.inshorts.ad

import app.revanced.util.returnEarly
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("com.nis.app")

    apply {
        inshortsAdsMethod.returnEarly()
    }
}
