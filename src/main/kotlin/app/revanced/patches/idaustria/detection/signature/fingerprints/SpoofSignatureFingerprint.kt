package app.revanced.patches.idaustria.detection.signature.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object SpoofSignatureFingerprint : MethodFingerprint(
    "L",
    parameters = listOf("L"),
    accessFlags = AccessFlags.PRIVATE.value,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/SL2Step1Task;") && methodDef.name == "getPubKey"
    }
)
