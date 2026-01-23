package app.revanced.patches.messenger.layout

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Hide Facebook button` by creatingBytecodePatch(
    description = "Hides the Facebook button in the top toolbar."
) {
    compatibleWith("com.facebook.orca")

    apply {
        isFacebookButtonEnabledMethod.returnEarly(false)
    }
}
