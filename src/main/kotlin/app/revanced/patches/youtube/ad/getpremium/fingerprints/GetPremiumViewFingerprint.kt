package app.revanced.patches.youtube.ad.getpremium.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

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
        classDef == "Lcom/google/android/apps/youtube/app/red/presenter/CompactYpcOfferModuleView;" &&
            methodDef.name == "onMeasure"
    }
}
