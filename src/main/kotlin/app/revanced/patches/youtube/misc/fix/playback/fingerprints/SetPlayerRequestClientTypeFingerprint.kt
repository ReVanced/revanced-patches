package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object SetPlayerRequestClientTypeFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IGET,
        Opcode.IPUT, // Sets ClientInfo.clientId.
    ),
    strings = listOf("10.29")
)
