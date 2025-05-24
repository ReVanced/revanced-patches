import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.fix.katanaProxyLoginMethodHandlerClassFingerprint
import app.revanced.patches.spotify.misc.fix.katanaProxyLoginMethodTryAuthorizeFingerprint
import app.revanced.util.returnEarly

@Suppress("unused")
val fixFacebookLoginPatch = bytecodePatch(
    name = "Fix Facebook login",
    description = "Fix logging in with Facebook by always opening the login in a web browser window.",
) {
    compatibleWith("com.spotify.music")

    execute {
        val katanaProxyLoginMethodHandlerClass = katanaProxyLoginMethodHandlerClassFingerprint.originalClassDef
        katanaProxyLoginMethodTryAuthorizeFingerprint
            .match(katanaProxyLoginMethodHandlerClass)
            .method
            .returnEarly(0)
    }
}
