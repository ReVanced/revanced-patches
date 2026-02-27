package app.revanced.patches.spotify.misc.fix.login

import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.gettingFirstImmutableMethod
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.katanaProxyLoginMethodHandlerClassMethod by gettingFirstImmutableMethod("katana_proxy_auth")

context(_: BytecodePatchContext)
internal fun ClassDef.getKatanaProxyLoginMethodTryAuthorizeMethod() = firstMethodDeclaratively("e2e") {
    instructions(0L())
}
