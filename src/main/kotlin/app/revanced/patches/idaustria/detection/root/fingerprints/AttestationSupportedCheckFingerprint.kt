package app.revanced.patches.idaustria.detection.root.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val attestationSupportedCheckFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC.value)
    returns("V")
    custom{ methodDef, _ ->
        methodDef.name == "attestationSupportCheck" &&
                methodDef.definingClass.endsWith("/DeviceIntegrityCheck;")
    }
}
