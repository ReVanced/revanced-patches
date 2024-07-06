package app.revanced.patches.com.eclipsim.gpsstatus2.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object LoadPreferencesFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.CONST_WIDE_16,
        Opcode.CONST_STRING,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.SPUT_WIDE,
    ),
    strings = listOf(
        "z_pref",
    ),
    parameters = listOf(
        "Landroid/content/Context;",
    ),
)
