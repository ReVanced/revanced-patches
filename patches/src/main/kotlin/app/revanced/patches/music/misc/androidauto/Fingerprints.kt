package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.checkCertificateMethod by gettingFirstMethodDeclaratively(
    "X509",
) {
    returnType("Z")
    parameterTypes("Ljava/lang/String;")
    instructions("Failed to get certificate"(String::contains))
}

internal val BytecodePatchContext.searchMediaItemsConstructorMethod by gettingFirstMethodDeclaratively(
    "ytm_media_browser/search_media_items",
) {
    returnType("V")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getSearchMediaItemsExecuteMethod() = firstMethodDeclaratively {
    parameterTypes()
}
