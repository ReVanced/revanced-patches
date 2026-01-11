package app.revanced.patches.googlephotos.misc.extension

import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private var getApplicationContextIndex = -1

internal val homeActivityInitHook = extensionHook(
    getInsertIndex = {
        getApplicationContextIndex = indexOfFirstInstructionOrThrow {
            getReference<MethodReference>()?.name == "getApplicationContext"
        }

        getApplicationContextIndex + 2 // Below the move-result-object instruction.
    },
    getContextRegister = {
        val moveResultInstruction = instructions.elementAt(getApplicationContextIndex + 1) as OneRegisterInstruction
        "v${moveResultInstruction.registerA}"
    },
) {
    name("onCreate")
    definingClass("/HomeActivity;"::endsWith)
    instructions(
        Opcode.CONST_STRING(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.IF_NEZ(),
        Opcode.INVOKE_VIRTUAL(), // Calls getApplicationContext().
        Opcode.MOVE_RESULT_OBJECT(),
    )
}
