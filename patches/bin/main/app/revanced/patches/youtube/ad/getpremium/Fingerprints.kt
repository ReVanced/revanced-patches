package app.revanced.patches.youtube.ad.getpremium

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val getPremiumViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("I", "I")
    opcodes(
        Opcode.ADD_INT_2ADDR,
        Opcode.ADD_INT_2ADDR,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    custom { method, _ ->
        method.name == "onMeasure" &&
            method.definingClass == "Lcom/google/android/apps/youtube/app/red/presenter/CompactYpcOfferModuleView;"
    }
}
