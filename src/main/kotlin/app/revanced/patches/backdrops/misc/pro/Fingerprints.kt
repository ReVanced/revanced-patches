package app.revanced.patches.backdrops.misc.pro

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint.methodFingerprint

internal val proUnlockFingerprint = methodFingerprint {
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ
    )
    custom { methodDef, classDef ->
        classDef.type == "Lcom/backdrops/wallpapers/data/local/DatabaseHandlerIAB;"
                && methodDef.name == "lambda\$existPurchase\$0"
    }
}