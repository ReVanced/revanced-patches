package app.revanced.patches.amazon

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.deepLinkingMethod by gettingFirstMethodDeclaratively(
    "https://www.",
    "android.intent.action.VIEW"
) {
    accessFlags(AccessFlags.PRIVATE)
    returnType("Z")
    parameterTypes("L")
}
