package app.revanced.patches.youtube.ad.getpremium

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val getPremiumViewFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("I", "I")
    opcodes(
        Opcode.ADD_INT_2ADDR,
        Opcode.ADD_INT_2ADDR,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    custom { methodDef, classDef ->
        classDef.type == "Lcom/google/android/apps/youtube/app/red/presenter/CompactYpcOfferModuleView;" &&
                (methodDef.name == "onMeasure")
    }
}
