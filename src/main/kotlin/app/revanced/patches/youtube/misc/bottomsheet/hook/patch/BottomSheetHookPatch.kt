package app.revanced.patches.youtube.misc.bottomsheet.hook.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.misc.bottomsheet.hook.fingerprints.CreateBottomSheetFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@DependsOn([IntegrationsPatch::class, BottomSheetHookResourcePatch::class])
class BottomSheetHookPatch : BytecodePatch(
    listOf(CreateBottomSheetFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        CreateBottomSheetFingerprint.result?.let {
            it.mutableMethod.apply {
                val returnLinearLayoutIndex = implementation!!.instructions.lastIndex
                val linearLayoutRegister = getInstruction<OneRegisterInstruction>(returnLinearLayoutIndex).registerA

                addHook = { classDescriptor ->
                    addInstruction(
                        returnLinearLayoutIndex,
                        "invoke-static { v$linearLayoutRegister }, " +
                                "${classDescriptor}->" +
                                "onFlyoutMenuCreate(Landroid/widget/LinearLayout;)V"
                    )
                }
            }
        } ?: throw CreateBottomSheetFingerprint.exception
    }

    internal companion object {
        internal lateinit var addHook: (String) -> Unit
            private set
    }
}