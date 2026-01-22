package app.revanced.patches.irplus.ad

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.irplusAdsMethod by gettingFirstMutableMethodDeclaratively("TAGGED") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("L", "Z")
}