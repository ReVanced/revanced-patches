package app.revanced.patches.spotify.misc.fix.login

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Fix Facebook login` by creatingBytecodePatch(
    description =
    "Fix logging in with Facebook when the app is patched by always opening the login in a web browser window.",
) {
    compatibleWith("com.spotify.music")

    apply {
        // The Facebook SDK tries to handle the login using the Facebook app in case it is installed.
        // However, the Facebook app does signature checks with the app that is requesting the authentication,
        // which ends up making the Facebook server reject with an invalid key hash for the app signature.
        // Override the Facebook SDK to always handle the login using the web browser, which does not perform
        // signature checks.

        val katanaProxyLoginMethodHandlerClass = katanaProxyLoginMethodHandlerClassMethod.originalClassDef
        // Always return 0 (no Intent was launched) as the result of trying to authorize with the Facebook app to
        // make the login fallback to a web browser window.
        katanaProxyLoginMethodTryAuthorizeMethod
            .match(katanaProxyLoginMethodHandlerClass)
            .method
            .returnEarly(0)
    }
}
