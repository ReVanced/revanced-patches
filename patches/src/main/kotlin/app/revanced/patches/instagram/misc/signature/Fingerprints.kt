package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val BytecodePatchContext.isValidSignatureClassMethod by gettingFirstMethodDeclaratively {
    strings("The provider for uri '", "' is not trusted: ")
}

internal val BytecodePatchContext.isValidSignatureMethodMethod by gettingFirstMethodDeclaratively {
    parameterTypes("L", "Z")
    returnType("Z")
    custom { method, _ ->
        method.indexOfFirstInstruction {
            getReference<MethodReference>()?.name == "keySet"
        } >= 0
    }
}
