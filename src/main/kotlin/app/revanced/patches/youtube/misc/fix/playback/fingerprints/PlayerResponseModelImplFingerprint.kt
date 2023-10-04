package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.extensions.containsConstantInstructionValue
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object PlayerResponseModelImplFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.RETURN_OBJECT,
        Opcode.CONST_4,
        Opcode.RETURN_OBJECT
    ),
    customFingerprint = handler@{ methodDef, _ ->
        if (!methodDef.definingClass.endsWith("/PlayerResponseModelImpl;")) return@handler false

        methodDef.containsConstantInstructionValue(55735497)
    }
)