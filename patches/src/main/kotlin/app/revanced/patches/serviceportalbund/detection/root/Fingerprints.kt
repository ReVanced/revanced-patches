package app.revanced.patches.serviceportalbund.detection.root

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val rootDetectionFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("V")
    custom { _, classDef ->
        classDef.endsWith("/DeviceIntegrityCheck;")
    }
}