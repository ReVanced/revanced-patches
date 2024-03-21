package app.revanced.patches.youtube.misc.navigation.utils

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal object InjectionUtils {
    const val REGISTER_TEMPLATE_REPLACEMENT: String = "REGISTER_INDEX"

    /**
     * Injects an instruction into insertIndex of the hook.
     * @param hook The hook to insert.
     * @param insertIndex The index to insert the instruction at.
     * a [OneRegisterInstruction] must be present before [insertIndex].
     */
    fun MutableMethod.injectHook(insertIndex: Int, hook: String) {
        // Register to pass to the hook
        val registerIndex = insertIndex - 1 // MOVE_RESULT_OBJECT is always the previous instruction
        val register = getInstruction<OneRegisterInstruction>(registerIndex).registerA

        addInstruction(
            insertIndex,
            hook.replace("REGISTER_INDEX", register.toString())
        )
    }
}