package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object CreatePlaybackSpeedMenuItemFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    opcodes = listOf(
        Opcode.IGET_OBJECT, // First instruction of the method
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        null // MOVE_RESULT or MOVE_RESULT_OBJECT, Return value controls the creation of the playback speed menu item.
    ),
    // 19.01 and earlier is missing the second parameter.
    // Since this fingerprint is somewhat weak, work around by checking for both method parameter signatures.
    customFingerprint = custom@{ methodDef, _ ->
        // 19.01 and earlier parameters are: "[L"
        // 19.02+ parameters are "[L", "F"
        val parameterTypes = methodDef.parameterTypes
        val firstParameter = parameterTypes.firstOrNull()

        if (firstParameter == null || !firstParameter.startsWith("[L")) {
            return@custom false
        }

        parameterTypes.size == 1 || (parameterTypes.size == 2 && parameterTypes[1] == "F")
    }
)
