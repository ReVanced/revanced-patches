package app.revanced.patches.tiktok.misc.login.fixgoogle

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.googleAuthAvailableMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/bytedance/lobby/google/GoogleAuth;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.googleOneTapAuthAvailableMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/bytedance/lobby/google/GoogleOneTapAuth;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
}