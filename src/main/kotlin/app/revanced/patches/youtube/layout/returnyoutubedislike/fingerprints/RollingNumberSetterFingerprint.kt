package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object RollingNumberSetterFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_OBJECT
    ),
    // Partial string match.
    strings = listOf("RollingNumberType required properties missing! Need")
)