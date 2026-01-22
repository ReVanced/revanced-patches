package app.revanced.patches.idaustria.detection.signature

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.spoofSignatureMethod by gettingFirstMutableMethodDeclaratively {
    name("getPubKey")
    definingClass("/SL2Step1Task;"::endsWith)
    accessFlags(AccessFlags.PRIVATE)
    returnType("L")
    parameterTypes("L")
}
