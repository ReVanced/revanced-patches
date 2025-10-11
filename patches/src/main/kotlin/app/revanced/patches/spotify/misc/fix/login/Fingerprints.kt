package app.revanced.patches.spotify.misc.fix.login

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.util.literal

internal val katanaProxyLoginMethodHandlerClassFingerprint by fingerprint {
    strings("katana_proxy_auth")
}

internal val katanaProxyLoginMethodTryAuthorizeFingerprint by fingerprint {
    strings("e2e")
    literal { 0 }
}
