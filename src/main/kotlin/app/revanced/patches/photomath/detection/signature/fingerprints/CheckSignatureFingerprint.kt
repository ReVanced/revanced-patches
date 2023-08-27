package app.revanced.patches.photomath.detection.signature.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object CheckSignatureFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        (methodDef.definingClass == "Lcom/microblink/photomath/main/activity/LauncherActivity;" ||
                methodDef.definingClass == "Lcom/microblink/photomath/PhotoMath;") &&
                methodDef.name == "onCreate"
    },
    strings = listOf(
        "currentSignature"
    ),
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
    )
)
