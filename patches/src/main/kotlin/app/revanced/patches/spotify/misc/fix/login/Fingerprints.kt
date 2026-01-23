package app.revanced.patches.spotify.misc.fix.login

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.literal

internal val BytecodePatchContext.katanaProxyLoginMethodHandlerClassMethod by gettingFirstMethodDeclaratively {
    strings("katana_proxy_auth")
}

internal val BytecodePatchContext.katanaProxyLoginMethodTryAuthorizeMethod by gettingFirstMethodDeclaratively {
    strings("e2e")
    literal { 0 }
}
