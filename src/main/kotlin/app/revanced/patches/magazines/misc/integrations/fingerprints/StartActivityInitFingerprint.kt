package app.revanced.patches.magazines.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal object StartActivityInitFingerprint : IntegrationsFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_BOOLEAN,
        Opcode.INVOKE_VIRTUAL, // invoke-virtual {p0}, Lcom/google/apps/dots/android/newsstand/activity/StartActivity;->getApplicationContext()Landroid/content/Context;
        Opcode.MOVE_RESULT_OBJECT, // move-result-object v2
    ),
    insertIndexResolver = { method ->
        method.implementation!!.instructions.indexOfFirst { it.getReference<MethodReference>().toString().contains("getApplicationContext") } + 2 // put this below move-result-object v2
    },
    contextRegisterResolver = { method ->
        ((method.implementation!!.instructions.toList()[method.implementation!!.instructions.indexOfFirst { it.getReference<MethodReference>().toString().contains("getApplicationContext") } + 1] as BuilderInstruction11x).registerA) // get from move-result-object v2
    },
    customFingerprint = { methodDef, _ -> methodDef.definingClass == "Lcom/google/apps/dots/android/newsstand/activity/StartActivity;" && methodDef.name == "onCreate" },
)
