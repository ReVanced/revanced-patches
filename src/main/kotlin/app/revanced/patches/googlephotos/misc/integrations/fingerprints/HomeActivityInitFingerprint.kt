package app.revanced.patches.googlephotos.misc.integrations.fingerprints

import app.revanced.patches.googlephotos.misc.integrations.fingerprints.HomeActivityInitFingerprint.getApplicationContextIndex
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal object HomeActivityInitFingerprint : IntegrationsFingerprint(
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_NEZ,
        Opcode.INVOKE_VIRTUAL, // Calls getApplicationContext().
        Opcode.MOVE_RESULT_OBJECT,
    ),
    insertIndexResolver = { method ->
        getApplicationContextIndex = method.indexOfFirstInstructionOrThrow {
            getReference<MethodReference>()?.name == "getApplicationContext"
        }

        getApplicationContextIndex + 2 // Below the move-result-object instruction.
    },
    contextRegisterResolver = { method ->
        val moveResultInstruction = method.implementation!!.instructions.elementAt(getApplicationContextIndex + 1)
            as OneRegisterInstruction
        moveResultInstruction.registerA
    },
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("/HomeActivity;")
    },
) {
    private var getApplicationContextIndex = -1
}
