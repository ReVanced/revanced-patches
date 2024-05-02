package app.revanced.patches.serviceportalbund.detection.root.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val rootDetectionFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC)
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/DeviceIntegrityCheck;")
    }
}