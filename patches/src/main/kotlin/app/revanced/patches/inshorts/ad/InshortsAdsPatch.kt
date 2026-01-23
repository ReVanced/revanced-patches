package app.revanced.patches.inshorts.ad

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("com.nis.app")

    apply {
        inshortsAdsMethod.returnEarly()
    }
}
