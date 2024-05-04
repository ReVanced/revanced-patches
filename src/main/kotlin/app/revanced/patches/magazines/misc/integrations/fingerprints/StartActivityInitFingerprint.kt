package app.revanced.patches.magazines.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

private var index: Int = -1
private var instructions: List<Instruction> = listOf()

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
        instructions = method.toMutable().getInstructions()
        index = instructions.indexOfFirst {
            return@indexOfFirst it.getReference<MethodReference>()?.name?.contains("getApplicationContext") == true
        }
        if(index == -1) {
            throw PatchException("Error finding the application context.")
        }
        index + 2 // put this below move-result-object v2
    },
    contextRegisterResolver = { _ ->
        ((instructions[index + 1] as BuilderInstruction11x).registerA) // get move-result-object v2 using the provided index
    },
    customFingerprint = { methodDef, _ -> methodDef.definingClass == "Lcom/google/apps/dots/android/newsstand/activity/StartActivity;" && methodDef.name == "onCreate" },
)

