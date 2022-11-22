package app.revanced.patches.youtube.layout.pivotbar.createbutton.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object PivotBarCreateButtonViewFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_DIRECT_RANGE, // unique instruction anchor
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC
    )
)