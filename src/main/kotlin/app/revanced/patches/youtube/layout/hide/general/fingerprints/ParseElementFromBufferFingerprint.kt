package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val parseElementFromBufferFingerprint = methodFingerprint {
    parameters("L", "L", "[B", "L", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
    )
    strings("Failed to parse Element") // String is a partial match.
}
