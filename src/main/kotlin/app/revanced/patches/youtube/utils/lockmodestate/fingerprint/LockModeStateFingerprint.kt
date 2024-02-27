package app.revanced.patches.youtube.utils.lockmodestate.fingerprint

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object LockModeStateFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC.value,
    parameters = emptyList(),
    opcodes = listOf(Opcode.RETURN_OBJECT),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "getLockModeStateEnum"
    }
)