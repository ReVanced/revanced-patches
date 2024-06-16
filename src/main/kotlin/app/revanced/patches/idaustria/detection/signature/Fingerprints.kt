package app.revanced.patches.idaustria.detection.signature

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val spoofSignatureFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("L")
    parameters("L")
    custom { methodDef, classDef ->
        classDef.endsWith("/SL2Step1Task;") && methodDef.name == "getPubKey"
    }
}