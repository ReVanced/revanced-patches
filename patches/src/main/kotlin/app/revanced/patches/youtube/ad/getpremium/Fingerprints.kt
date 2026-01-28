package app.revanced.patches.youtube.ad.getpremium

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val getPremiumViewMethodMatch = firstMethodComposite {
    name("onMeasure")
    definingClass("Lcom/google/android/apps/youtube/app/red/presenter/CompactYpcOfferModuleView;")
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("I", "I")
    opcodes(
        Opcode.ADD_INT_2ADDR,
        Opcode.ADD_INT_2ADDR,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
}
