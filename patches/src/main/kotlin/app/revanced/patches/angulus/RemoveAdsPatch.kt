package app.revanced.patches.angulus

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val angulusPatch = bytecodePatch(name = "Hide ads") {
    compatibleWith("com.drinkplusplus.angulus")

    execute {
        // Always returns 0 as the daily measurement count
        angulusAdsFingerprint.method.returnEarly()
    }

}
