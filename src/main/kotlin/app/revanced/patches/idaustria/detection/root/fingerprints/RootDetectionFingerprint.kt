package app.revanced.patches.idaustria.detection.root.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object RootDetectionFingerprint : MethodFingerprint(
    "V",
    access = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/DeviceIntegrityCheck;")
    }
)
