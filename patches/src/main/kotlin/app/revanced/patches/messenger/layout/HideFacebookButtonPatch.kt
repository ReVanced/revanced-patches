package app.revanced.patches.messenger.layout

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideFacebookButtonPatch = bytecodePatch(
    name = "Hide Facebook button",
    description = "Hides the Facebook button in the top toolbar."
) {
    compatibleWith("com.facebook.orca")

    execute {
        isFacebookButtonEnabledFingerprint.method.returnEarly(false)
    }
}
