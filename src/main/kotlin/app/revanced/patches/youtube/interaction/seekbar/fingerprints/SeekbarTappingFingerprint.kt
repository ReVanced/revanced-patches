package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object SeekbarTappingFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        // Insert seekbar tapping instructions here.
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
    ),
    customFingerprint = custom@{ methodDef, _ ->
        if (methodDef.name != "onTouchEvent") return@custom false

        methodDef.containsWideLiteralInstructionValue(Integer.MAX_VALUE.toLong())
    }
)