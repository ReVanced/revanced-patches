package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object RollingNumberSetterFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_OBJECT
    ),
    strings = listOf("RollingNumberType required properties missing! Need updateCount, fontName, color and fontSize.")
)