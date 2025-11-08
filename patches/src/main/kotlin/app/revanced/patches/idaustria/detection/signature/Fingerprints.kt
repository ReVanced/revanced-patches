package app.revanced.patches.idaustria.detection.signature

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val spoofSignatureFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("L")
    parameters("L")
    custom { method, classDef ->
        classDef.endsWith("/SL2Step1Task;") && method.name == "getPubKey"
    }
}
