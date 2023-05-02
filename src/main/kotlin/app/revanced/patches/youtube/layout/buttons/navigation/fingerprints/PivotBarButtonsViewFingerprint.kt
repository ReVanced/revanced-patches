package app.revanced.patches.youtube.layout.buttons.navigation.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object PivotBarButtonsViewFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL_RANGE,
        Opcode.MOVE_RESULT_OBJECT, // target reference
        Opcode.GOTO,
    )
)