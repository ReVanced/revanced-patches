package app.revanced.patches.photoshopmix

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val bypassLoginPatch = bytecodePatch(
    name = "Bypass login",
    description = "Allows the use of the app after its discontinuation.",
) {
    compatibleWith("com.adobe.photoshopmix")

    apply {
        isLoggedInMethod.returnEarly(true)

        // Disables these buttons that cause the app to crash while not logged in.
        ccLibButtonClickHandlerMethod.returnEarly()
        lightroomButtonClickHandlerMethod.returnEarly()
        ccButtonClickHandlerMethod.returnEarly()
    }
}
