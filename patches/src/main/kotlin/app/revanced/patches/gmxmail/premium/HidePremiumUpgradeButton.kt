package app.revanced.patches.gmxmail.premium

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

val hidePremiumUpgradeButtonPatch = bytecodePatch(
    name = "Hide Premium upgrade button",
    description = "Hides the Premium upgrade button in the navigation drawer.",
) {
    compatibleWith("de.gmx.mobile.android.mail")

    execute {
        isUpsellingPossibleFingerprint.method.returnEarly(false)
    }
}
