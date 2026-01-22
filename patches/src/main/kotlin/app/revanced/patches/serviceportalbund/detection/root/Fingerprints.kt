package app.revanced.patches.serviceportalbund.detection.root

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.rootDetectionMethod by gettingFirstMutableMethodDeclaratively {
    definingClass("/DeviceIntegrityCheck;"::endsWith)
    accessFlags(AccessFlags.PUBLIC)
    returnType("V")
}
