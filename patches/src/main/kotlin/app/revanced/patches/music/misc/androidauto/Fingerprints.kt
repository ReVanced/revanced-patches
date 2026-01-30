package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.checkCertificateMethod by gettingFirstMutableMethodDeclaratively(
    "X509",
) {
    returnType("Z")
    parameterTypes("Ljava/lang/String;")
    instructions("Failed to get certificate"(String::contains))
}

internal val BytecodePatchContext.searchMediaItemsConstructorMethod by gettingFirstMutableMethodDeclaratively(
    "ytm_media_browser/search_media_items",
) {
    returnType("V")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getSearchMediaItemsExecuteMethod() = firstMutableMethodDeclaratively {
    parameterTypes()
}
