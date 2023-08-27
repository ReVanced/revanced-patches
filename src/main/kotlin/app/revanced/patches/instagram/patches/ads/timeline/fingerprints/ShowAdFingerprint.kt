package app.revanced.patches.instagram.patches.ads.timeline.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object ShowAdFingerprint : MethodFingerprint(
    "Z",
    AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.FINAL,
    listOf("L", "L", "Z", "Z"),
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.RETURN,
        Opcode.CONST_4,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.GOTO,
        Opcode.CONST_4,
    ),
)
