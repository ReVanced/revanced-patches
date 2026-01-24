package app.revanced.patches.inshorts.ad

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("com.nis.app")

    apply {
        inshortsAdsMethod.returnEarly()
    }
}
