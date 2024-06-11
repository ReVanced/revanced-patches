package app.revanced.patches.idaustria.detection.signature

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val spoofSignatureFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("L")
    parameters("L")
    custom { methodDef, classDef ->
        classDef.endsWith("/SL2Step1Task;") && methodDef.name == "getPubKey"
    }
}