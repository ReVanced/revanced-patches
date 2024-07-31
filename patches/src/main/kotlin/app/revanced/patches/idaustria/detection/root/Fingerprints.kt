package app.revanced.patches.idaustria.detection.root

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val attestationSupportedCheckFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("V")
    custom { method, classDef ->
        method.name == "attestationSupportCheck" &&
            classDef.endsWith("/DeviceIntegrityCheck;")
    }
}

internal val bootloaderCheckFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    custom { method, classDef ->
        method.name == "bootloaderCheck" &&
            classDef.endsWith("/DeviceIntegrityCheck;")
    }
}

internal val rootCheckFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("V")
    custom { method, classDef ->
        method.name == "rootCheck" &&
            classDef.endsWith("/DeviceIntegrityCheck;")
    }
}
