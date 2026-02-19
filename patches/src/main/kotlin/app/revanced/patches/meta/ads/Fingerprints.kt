package app.revanced.patches.meta.ads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.adInjectorMethod by gettingFirstMethodDeclaratively(
    "SponsoredContentController.insertItem",
) {
    accessFlags(AccessFlags.PRIVATE)
    returnType("Z")
    parameterTypes("L", "L")
}
