package app.revanced.patches.youtube.layout.autoplaybutton.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object AutoNavInformerFingerprint : MethodFingerprint(
    "Z",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    ),
    customFingerprint = { it.definingClass.endsWith("WillAutonavInformer;") }
)