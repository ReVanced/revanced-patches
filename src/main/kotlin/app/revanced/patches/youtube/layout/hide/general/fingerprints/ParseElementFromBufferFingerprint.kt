package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object ParseElementFromBufferFingerprint : MethodFingerprint(
    parameters = listOf("L","L","[B", "L","L"),
    opcodes = listOf(Opcode.INVOKE_INTERFACE, Opcode.MOVE_RESULT_OBJECT),
    strings = listOf("Failed to parse Element")
)