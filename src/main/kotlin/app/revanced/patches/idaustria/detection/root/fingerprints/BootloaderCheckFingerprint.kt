package app.revanced.patches.idaustria.detection.root.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val bootloaderCheckFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    custom { methodDef, _ ->
        methodDef.name == "bootloaderCheck" &&
        methodDef.definingClass.endsWith("/DeviceIntegrityCheck;")
    }
}
