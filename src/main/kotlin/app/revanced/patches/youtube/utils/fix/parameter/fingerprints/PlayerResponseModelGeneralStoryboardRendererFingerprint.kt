package app.revanced.patches.youtube.utils.fix.parameter.fingerprints

import app.revanced.util.containsWideLiteralInstructionIndex
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object PlayerResponseModelGeneralStoryboardRendererFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.RETURN_OBJECT,
        Opcode.CONST_4,
        Opcode.RETURN_OBJECT
    ),
    customFingerprint = handler@{ methodDef, _ ->
        if (!methodDef.definingClass.endsWith("/PlayerResponseModelImpl;"))
            return@handler false

        methodDef.containsWideLiteralInstructionIndex(55735497)
    }
)