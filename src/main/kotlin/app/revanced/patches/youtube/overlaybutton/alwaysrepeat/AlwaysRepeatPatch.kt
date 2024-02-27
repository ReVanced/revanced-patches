package app.revanced.patches.youtube.overlaybutton.alwaysrepeat

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.overlaybutton.alwaysrepeat.fingerprints.AutoNavInformerFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

object AlwaysRepeatPatch : BytecodePatch(
    setOf(AutoNavInformerFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        AutoNavInformerFingerprint.result?.let {
            with(
                context
                    .toMethodWalker(it.method)
                    .nextMethod(it.scanResult.patternScanResult!!.startIndex, true)
                    .getMethod() as MutableMethod
            ) {
                val index = implementation!!.instructions.size - 1 - 1
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructions(
                    index + 1, """
                        invoke-static {v$register}, $UTILS_PATH/AlwaysRepeatPatch;->enableAlwaysRepeat(Z)Z
                        move-result v0
                        """
                )
            }
        } ?: throw AutoNavInformerFingerprint.exception

    }
}
