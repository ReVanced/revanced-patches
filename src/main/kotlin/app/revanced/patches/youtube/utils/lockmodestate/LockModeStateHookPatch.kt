package app.revanced.patches.youtube.utils.lockmodestate

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.lockmodestate.fingerprint.LockModeStateFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

object LockModeStateHookPatch : BytecodePatch(
    setOf(LockModeStateFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        LockModeStateFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex + 1, """
                        invoke-static {v$insertRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->setLockModeState(Ljava/lang/Enum;)V
                        return-object v$insertRegister
                        """
                )
                removeInstruction(insertIndex)
            }
        } ?: throw LockModeStateFingerprint.exception

    }

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$UTILS_PATH/LockModeStateHookPatch;"
}
