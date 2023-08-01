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
        Opcode.MOVE_OBJECT_FROM16, // available unused register
        Opcode.MOVE_OBJECT_FROM16,
        null, // move-object/from16 or move/from16
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.INVOKE_VIRTUAL, // CharSequence atomic reference
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.MOVE_OBJECT, // CharSequence reference, and control flow label. Insert code here.
        null, // invoke-interface or invoke-virtual
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        null, // invoke-interface or invoke-virtual
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.GOTO,
    )
)