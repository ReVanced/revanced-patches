package app.revanced.patches.youtube.layout.hide.getpremium.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object GetPremiumViewFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.ADD_INT_2ADDR,
        Opcode.ADD_INT_2ADDR,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lcom/google/android/apps/youtube/app/red/presenter/CompactYpcOfferModuleView;"
                && methodDef.name == "onMeasure"
    }
)