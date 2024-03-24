package app.revanced.patches.youtube.misc.navigation.utils

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
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

    /**
     * @param insertionFilter Filter that identifies method calls with a non void return type.
     */
    fun MutableMethod.injectHooksByFilter(
        insertionFilter: (BuilderInstruction) -> Boolean,
        hook: String
    ) {
        val methodInstructions = implementation!!.instructions
        methodInstructions.filter(insertionFilter).let { filteredInstructions ->
            if (filteredInstructions.isEmpty()) throw PatchException("Could not find insertion indexes")
            filteredInstructions.forEach {
                val index = methodInstructions.indexOf(it)
                val register = (getInstruction(index + 1) as OneRegisterInstruction).registerA
                addInstruction(
                    index + 2,
                    hook.replace("REGISTER_INDEX", register.toString()),
                )
            }
        }
    }
}