package app.revanced.patches.gmxmail.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides sponsored ads.",
) {
    compatibleWith("de.gmx.mobile.android.mail")

    execute {
        getAdvertisementStatusFingerprint.method.returnEarly(2)
    }
}
