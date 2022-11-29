package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object IsInOfflineModeCheckResultFingerprint : MethodFingerprint(
    "L",
    parameters = listOf("L", "L", "L", "L", "L"),
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.NEW_INSTANCE,
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_OBJECT,
    ),
)