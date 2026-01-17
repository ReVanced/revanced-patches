package app.revanced.patches.orfon.detection.root

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isDeviceRootedMethod by gettingFirstMutableMethodDeclaratively {
    name("isDeviceRooted")
    definingClass("/RootChecker;"::endsWith)
    accessFlags(AccessFlags.PUBLIC)
    returnType("Z")
}
