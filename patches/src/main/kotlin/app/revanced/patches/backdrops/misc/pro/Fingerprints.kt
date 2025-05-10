package app.revanced.patches.backdrops.misc.pro

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val proUnlockFingerprint by fingerprint {
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
    )
    custom { method, _ ->
        method.name == "lambda\$existPurchase\$0" &&
            method.definingClass == "Lcom/backdrops/wallpapers/data/local/DatabaseHandlerIAB;"
    }
}
