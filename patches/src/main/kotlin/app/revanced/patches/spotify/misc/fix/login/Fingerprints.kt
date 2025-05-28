package app.revanced.patches.spotify.misc.fix.login

import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val katanaProxyLoginMethodHandlerClassFingerprint = fingerprint {
    strings("katana_proxy_auth")
}

internal val katanaProxyLoginMethodTryAuthorizeFingerprint = fingerprint {
    strings("e2e")
    literal { 0 }
}
