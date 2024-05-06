package app.revanced.patches.idaustria.detection.root.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val rootCheckFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC.value)
    custom{ methodDef, _ ->
        methodDef.name == "rootCheck" &&
                methodDef.definingClass.endsWith("/DeviceIntegrityCheck;")
    }
}
