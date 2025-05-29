package app.revanced.patches.solidexplorer2.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes ads from the app"
) {
    compatibleWith("pl.solidexplorer2")

    execute {
        checkLicenceOnBackendFingerprint.method.returnEarly(true)
    }
}