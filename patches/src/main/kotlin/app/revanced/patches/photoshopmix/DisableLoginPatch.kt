package app.revanced.patches.photoshopmix

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableLoginPatch = bytecodePatch(
    name = "Disable login",
    description = "Allows you to use the app after its discontinuation",
    use = false,
) {
    compatibleWith("com.adobe.photoshopmix")

    execute {
        disableLoginFingerprint.method.returnEarly(true)

        // Disables these buttons that cause the app to crash while not logged in
        libButtonClickedFingerprint.method.returnEarly()
        lightroomButtonClickedFingerprint.method.returnEarly()
        ccButtonClickedFingerprint.method.returnEarly()
    }
}
