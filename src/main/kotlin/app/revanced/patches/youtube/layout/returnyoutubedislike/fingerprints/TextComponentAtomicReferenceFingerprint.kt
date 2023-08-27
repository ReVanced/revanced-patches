package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Resolves against the same method that [TextComponentContextFingerprint] resolves to.
 */
object TextComponentAtomicReferenceFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PROTECTED or AccessFlags.FINAL,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.MOVE_OBJECT, // Register A and B is context, use B as context, reuse A as free register
        Opcode.INVOKE_VIRTUAL, // Register C is atomic reference
        Opcode.MOVE_RESULT_OBJECT, // Register A is char sequence
        Opcode.MOVE_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_INTERFACE, // Insert hook here
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.GOTO
    )
)