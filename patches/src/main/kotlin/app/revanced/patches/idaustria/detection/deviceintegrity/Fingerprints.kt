package app.revanced.patches.idaustria.detection.deviceintegrity

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val isDeviceBootloaderOpenFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/lang/Object;")
    custom { method, classDef ->
        method.name == "isDeviceBootloaderOpen" &&
                classDef.endsWith("/DeviceIntegrityCheckProviderImpl;")
    }
}

internal val isDeviceRootedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    custom { method, classDef ->
        method.name == "isDeviceRooted" &&
                classDef.endsWith("/DeviceIntegrityCheckProviderImpl;")
    }
}
