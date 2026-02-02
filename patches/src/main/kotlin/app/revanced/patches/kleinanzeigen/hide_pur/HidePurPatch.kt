package app.revanced.patches.kleinanzeigen.hide_pur

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hidePurPatch = bytecodePatch(
    name = "Hide PUR",
    description = "Hides PUR (Ad Free Subscription) from Settings Menu.",
) {
    compatibleWith("com.ebay.kleinanzeigen")

    execute {
        getShowAdFreeSubscriptionFingerprint.method.returnEarly(false)
    }
}
