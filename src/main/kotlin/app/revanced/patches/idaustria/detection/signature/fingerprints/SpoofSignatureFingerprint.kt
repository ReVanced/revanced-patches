package app.revanced.patches.idaustria.detection.signature.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val spoofSignatureFingerprint = methodFingerprint {
    returns("L")
    parameters("L")
    accessFlags(AccessFlags.PRIVATE.value)
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/SL2Step1Task;") && methodDef.name == "getPubKey"
    }
}
