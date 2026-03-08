package app.revanced.patches.photoshopmix

import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val bypassLoginPatch = bytecodePatch(
    name = "Bypass login",
    description = "Allows you to use the app after its discontinuation",
    use = false,
) {
    compatibleWith("com.adobe.photoshopmix")

    apply {
        disableLoginMethod.returnEarly(true)

        // Disables these buttons that cause the app to crash while not logged in.
        libButtonClickedMethod.returnEarly()
        lightroomButtonClickedMethod.returnEarly()
        ccButtonClickedMethod.returnEarly()
    }
}
