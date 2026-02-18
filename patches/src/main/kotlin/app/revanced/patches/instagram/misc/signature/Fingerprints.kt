package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.ClassDef

context(_: BytecodePatchContext)
internal fun ClassDef.getIsValidSignatureClassMethod() = firstMethodDeclaratively(
    "The provider for uri '",
    "' is not trusted: ",
)

internal val BytecodePatchContext.isValidSignatureMethodMethod by gettingFirstMethodDeclaratively {
    parameterTypes("L", "Z")
    returnType("Z")
    instructions(method("keySet"))
}
