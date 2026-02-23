package app.revanced.patches.googlenews.misc.extension.hooks

import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private var getApplicationContextIndex = -1

internal val startActivityInitHook = extensionHook(
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
    definingClass("/StartActivity;")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_BOOLEAN,
        Opcode.INVOKE_VIRTUAL, // Calls startActivity.getApplicationContext().
        Opcode.MOVE_RESULT_OBJECT,
    )
}
