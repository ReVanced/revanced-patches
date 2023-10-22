package app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object LoginActivityOnCreateFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4
    ),
    customFingerprint = { method, classDef ->
        method.name == "onCreate" && classDef.type.endsWith("LoginActivity;")
    }
)