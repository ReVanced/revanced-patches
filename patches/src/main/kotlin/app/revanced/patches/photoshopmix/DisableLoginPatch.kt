package app.revanced.patches.photoshopmix

import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableLoginPatch = bytecodePatch(
    name = "Disable login",
    description = "Allows you to use the app after its discontinuation",
    use = false,
) {
    compatibleWith("com.adobe.photoshopmix")

    apply {
        var psMixFragment = "Lcom/adobe/photoshopmix/PSMixFragment;"
        var firstLoginMethod = firstMethod{ name == "isLoggedIn" && definingClass == "Lcom/adobe/acira/accreativecloudlibrary/CreativeCloudSource;" && returnType == "Z"}
        var libButtonClickedMethod = firstMethod{name=="ccLibButtonClickHandler" && definingClass==psMixFragment}
        var lightroomButtonClickedMethod = firstMethod{name=="lightroomButtonClickHandler" && definingClass==psMixFragment}
        var ccButtonClickedMethod = firstMethod{name=="ccButtonClickHandler" && definingClass==psMixFragment}

        firstLoginMethod.returnEarly(true)

        // Disables these buttons that cause the app to crash while not logged in
        libButtonClickedMethod.returnEarly()
        lightroomButtonClickedMethod.returnEarly()
        ccButtonClickedMethod.returnEarly()
    }
}
