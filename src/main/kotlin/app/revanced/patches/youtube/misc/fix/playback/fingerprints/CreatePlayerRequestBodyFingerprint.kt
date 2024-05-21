package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object CreatePlayerRequestBodyFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.CHECK_CAST,
        Opcode.IGET,
        Opcode.AND_INT_LIT16,
    ),
    strings = listOf("ms"),
)
