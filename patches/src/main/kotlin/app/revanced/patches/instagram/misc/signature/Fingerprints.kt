package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val isValidSignatureClassMethodMatch = firstMethodComposite(
    "The provider for uri '", "' is not trusted: ",
)

internal val BytecodePatchContext.isValidSignatureMethodMethod by gettingFirstMutableMethodDeclaratively {
    parameterTypes("L", "Z")
    returnType("Z")
    custom {
        indexOfFirstInstruction {
            getReference<MethodReference>()?.name == "keySet"
        } >= 0
    }
}
