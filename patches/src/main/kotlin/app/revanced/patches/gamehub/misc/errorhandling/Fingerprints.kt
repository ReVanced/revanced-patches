package app.revanced.patches.gamehub.misc.errorhandling

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

// B: NetErrorHandler$DefaultImpls — return-void before goto/16 to silence network error callbacks.
internal val netErrorHandlerFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/drake/net/interfaces/NetErrorHandler${'$'}DefaultImpls;" &&
            method.implementation?.instructions?.any { it.opcode == Opcode.GOTO_16 } == true
    }
}

// C: TipUtils.c(String) — suppress tip/toast popups.
internal val tipUtilsFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/drake/net/utils/TipUtils;" && method.name == "c"
    }
}


