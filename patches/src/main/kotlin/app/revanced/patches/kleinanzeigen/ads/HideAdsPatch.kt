package app.revanced.patches.kleinanzeigen.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides sponsored ads and Google Ads and disables Microsoft Clarity.",
) {
    compatibleWith("com.ebay.kleinanzeigen")

    execute {
        getLibertyInitFingerprint.method.returnEarly()
    }
}
