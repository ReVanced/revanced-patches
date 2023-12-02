package app.revanced.patches.idaustria.detection.root.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object RootCheckFingerprint : MethodFingerprint(
    "V",
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef, _ ->
        methodDef.name == "rootCheck" &&
        methodDef.definingClass.endsWith("/DeviceIntegrityCheck;")
    }
)
