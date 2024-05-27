package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object ParseElementFromBufferFingerprint : MethodFingerprint(
    parameters = listOf("L", "L", "[B", "L", "L"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT
    ),
    strings = listOf("Failed to parse Element") // String is a partial match.
)