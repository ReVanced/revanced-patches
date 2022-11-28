package app.revanced.patches.backdrops.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object ProUnlockFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ
    ),
    customFingerprint = { it.definingClass == "Lcom/backdrops/wallpapers/data/local/DatabaseHandlerIAB;" && it.name == "lambda\$existPurchase\$0" }
)