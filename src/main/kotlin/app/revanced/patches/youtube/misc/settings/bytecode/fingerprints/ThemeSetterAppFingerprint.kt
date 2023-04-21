package app.revanced.patches.youtube.misc.settings.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object ThemeSetterAppFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("L", "L", "L", "L"),
    opcodes = listOf(
        Opcode.CONST, // target reference
        Opcode.GOTO,
        Opcode.CONST, // target reference
        Opcode.INVOKE_DIRECT,
        Opcode.RETURN_OBJECT,
        Opcode.NEW_INSTANCE,
        null, // changed from invoke interface to invoke virtual
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.CONST, // target reference
    )
)