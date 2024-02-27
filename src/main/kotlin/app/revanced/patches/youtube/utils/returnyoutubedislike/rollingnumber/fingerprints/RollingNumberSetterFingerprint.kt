package app.revanced.patches.youtube.utils.returnyoutubedislike.rollingnumber.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

/**
 * This fingerprint is compatible with YouTube v18.29.38+
 */
object RollingNumberSetterFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.CHECK_CAST,
        Opcode.IGET,
        Opcode.AND_INT_LIT8
    ),
    strings = listOf("RollingNumberType required properties missing! Need updateCount, fontName, color and fontSize.")
)