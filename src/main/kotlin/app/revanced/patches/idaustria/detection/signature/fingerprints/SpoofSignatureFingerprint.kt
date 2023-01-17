package app.revanced.patches.idaustria.detection.signature.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object SpoofSignatureFingerprint : MethodFingerprint(
    "L",
    parameters = listOf("L"),
    access = AccessFlags.PRIVATE.value,
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/SL2Step1Task;") && methodDef.name == "getPubKey"
    }
)
