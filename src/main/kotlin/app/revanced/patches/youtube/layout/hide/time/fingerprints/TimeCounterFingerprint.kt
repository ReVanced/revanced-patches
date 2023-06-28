package app.revanced.patches.youtube.layout.hide.time.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object TimeCounterFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    returnType = "V",
    opcodes = listOf(
        Opcode.SUB_LONG_2ADDR,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IGET_WIDE,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    ),
    customFingerprint = { _, classDef ->
        // On older devices this fingerprint resolves very slowly.
        // Speed this up by checking for the number of methods.
        classDef.methods.count() == 14
    }
)