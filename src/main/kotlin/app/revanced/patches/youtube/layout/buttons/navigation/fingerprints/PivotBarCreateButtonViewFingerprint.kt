package app.revanced.patches.youtube.layout.buttons.navigation.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object PivotBarCreateButtonViewFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_DIRECT_RANGE,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC
    )
)