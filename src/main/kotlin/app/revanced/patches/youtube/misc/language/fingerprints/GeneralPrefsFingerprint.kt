package app.revanced.patches.youtube.misc.language.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

/**
 * Compatible with YouTube v18.33.40~
 */
object GeneralPrefsFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    strings = listOf("bedtime_reminder_toggle"),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("/GeneralPrefsFragment;") }
)