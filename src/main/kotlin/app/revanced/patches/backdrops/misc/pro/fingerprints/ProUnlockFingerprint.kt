package app.revanced.patches.backdrops.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object ProUnlockFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/backdrops/wallpapers/data/local/DatabaseHandlerIAB;"
                && methodDef.name == "lambda\$existPurchase\$0"
    }
)