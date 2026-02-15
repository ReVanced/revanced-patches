package app.revanced.patches.gmxmail.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides sponsored ads and removes the Premium upgrade button from the navigation drawer.",
) {
    compatibleWith("de.gmx.mobile.android.mail")

    execute {
        getAdvertisementStatusFingerprint.method.returnEarly(2)
        isUpsellingPossibleFingerprint.method.returnEarly(false)
    }
}
