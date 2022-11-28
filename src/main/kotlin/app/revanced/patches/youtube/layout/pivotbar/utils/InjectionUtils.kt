package app.revanced.patches.youtube.layout.pivotbar.utils

import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import org.jf.dexlib2.Opcode.MOVE_RESULT_OBJECT
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

internal object InjectionUtils {
    const val REGISTER_TEMPLATE_REPLACEMENT: String = "REGISTER_INDEX"

    /**
     * Injects an instruction into insertIndex of the hook.
     * @param hook The hook to insert.
     * @param insertIndex The index to insert the instruction at.
     * [MOVE_RESULT_OBJECT] has to be the previous instruction before [insertIndex].
     */
    fun MutableMethod.injectHook(hook: String, insertIndex: Int) {
        val injectTarget = this

        // Register to pass to the hook
        val registerIndex = insertIndex - 1 // MOVE_RESULT_OBJECT is always the previous instruction
        val register = (injectTarget.instruction(registerIndex) as OneRegisterInstruction).registerA

        injectTarget.addInstruction(
            insertIndex,
            hook.replace("REGISTER_INDEX", register.toString()),
        )
    }

    // TODO: move this to a global extension class and use it in all patches
    fun MethodFingerprint.toErrorResult() = PatchResultError("Failed to resolve $name")
}