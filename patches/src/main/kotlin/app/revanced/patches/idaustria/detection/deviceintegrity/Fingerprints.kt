package app.revanced.patches.idaustria.detection.deviceintegrity

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isDeviceBootloaderOpenMethod by gettingFirstMethodDeclaratively {
    name("isDeviceBootloaderOpen")
    definingClass("/DeviceIntegrityCheckProviderImpl;")
    accessFlags(AccessFlags.PUBLIC)
    returnType("Ljava/lang/Object;")
}

internal val BytecodePatchContext.isDeviceRootedMethod by gettingFirstMethodDeclaratively {
    name("isDeviceRooted")
    definingClass("/DeviceIntegrityCheckProviderImpl;")
    accessFlags(AccessFlags.PUBLIC)
    returnType("Z")
}
