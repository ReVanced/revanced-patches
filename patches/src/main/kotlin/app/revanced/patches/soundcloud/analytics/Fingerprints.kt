package app.revanced.patches.soundcloud.analytics

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.createTrackingApiMethod by gettingFirstMutableMethodDeclaratively(
    "backend",
    "boogaloo"
) {
    name("create")
    accessFlags(AccessFlags.PUBLIC)
    returnType("L")
}
