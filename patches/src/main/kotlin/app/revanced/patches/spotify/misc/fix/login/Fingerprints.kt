package app.revanced.patches.spotify.misc.fix.login

import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.gettingFirstMutableMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.katanaProxyLoginMethodHandlerClassMethod by gettingFirstMethod("katana_proxy_auth")

context(_: BytecodePatchContext)
internal fun ClassDef.getKatanaProxyLoginMethodTryAuthorizeMethod() = firstMutableMethodDeclaratively("e2e") {
    instructions(0L())
}
