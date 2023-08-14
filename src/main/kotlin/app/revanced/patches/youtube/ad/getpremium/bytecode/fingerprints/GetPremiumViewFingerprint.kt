package app.revanced.patches.youtube.ad.getpremium.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object GetPremiumViewFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PROTECTED or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("I", "I"),
    opcodes = listOf(
        Opcode.ADD_INT_2ADDR,
        Opcode.ADD_INT_2ADDR,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/google/android/apps/youtube/app/red/presenter/CompactYpcOfferModuleView;"
                && methodDef.name == "onMeasure"
    }
)