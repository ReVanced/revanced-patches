package app.revanced.patches.spotify.misc.fix.login

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.literal
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.katanaProxyLoginMethodHandlerClassMethod by gettingFirstMethodDeclaratively {
    strings("katana_proxy_auth")
}

internal val BytecodePatchContext.katanaProxyLoginMethodTryAuthorizeMethod by gettingFirstMethodDeclaratively {
    strings("e2e")
    literal { 0 }
}
