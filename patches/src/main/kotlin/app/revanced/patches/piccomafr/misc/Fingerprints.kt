package app.revanced.patches.piccomafr.misc

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.getAndroidIdMethod by gettingFirstMutableMethodDeclaratively(
    "context",
    "android_id"
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes("Landroid/content/Context;")
}
