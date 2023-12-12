package app.revanced.patches.serviceportalbund.detection.root.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object RootDetectionFingerprint : MethodFingerprint(
    "V",
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/DeviceIntegrityCheck;")
    }
)
