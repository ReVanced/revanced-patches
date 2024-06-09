package app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val loginActivityOnCreateFingerprint = methodFingerprint {
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
    )
    custom { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("LoginActivity;")
    }
}
