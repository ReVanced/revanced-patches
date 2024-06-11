package app.revanced.patches.idaustria.detection.root

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val attestationSupportedCheckFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("V")
    custom{ methodDef, classDef ->
        methodDef.name == "attestationSupportCheck" &&
                classDef.endsWith("/DeviceIntegrityCheck;")
    }
}

internal val bootloaderCheckFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    custom { methodDef, classDef ->
        methodDef.name == "bootloaderCheck" &&
        classDef.endsWith("/DeviceIntegrityCheck;")
    }
}

internal val rootCheckFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("V")
    custom{ methodDef, classDef ->
        methodDef.name == "rootCheck" &&
        classDef.endsWith("/DeviceIntegrityCheck;")
    }
}
