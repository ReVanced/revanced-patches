package app.revanced.patches.peacocktv.ads

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.mediaTailerAdServiceMethod by gettingFirstMethodDeclaratively("Could not build MT Advertising service") {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Ljava/lang/Object;")
}
