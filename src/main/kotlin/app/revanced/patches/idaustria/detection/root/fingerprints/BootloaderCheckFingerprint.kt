package app.revanced.patches.idaustria.detection.root.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val bootloaderCheckFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    custom { methodDef, classDef ->
        methodDef.name == "bootloaderCheck" &&
        classDef.endsWith("/DeviceIntegrityCheck;")
    }
}
