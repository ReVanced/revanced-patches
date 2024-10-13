package app.revanced.patches.googlephotos.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private var getApplicationContextIndex = -1

internal val homeActivityInitHook = extensionHook(
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
) {
    opcodes(
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_NEZ,
        Opcode.INVOKE_VIRTUAL, // Calls getApplicationContext().
        Opcode.MOVE_RESULT_OBJECT,
    )
    custom { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("/HomeActivity;")
    }
}
