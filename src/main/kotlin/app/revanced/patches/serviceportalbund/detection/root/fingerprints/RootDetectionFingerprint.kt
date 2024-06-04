package app.revanced.patches.serviceportalbund.detection.root.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val rootDetectionFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("V")
    custom { _, classDef ->
        classDef.endsWith("/DeviceIntegrityCheck;")
    }
}