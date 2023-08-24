package app.revanced.patches.youtube.layout.hide.loadmorebutton.bytecode.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.loadmorebutton.bytecode.fingerprints.HideLoadMoreButtonFingerprint
import app.revanced.patches.youtube.layout.hide.loadmorebutton.resource.patch.HideLoadMoreButtonResourcePatch
import app.revanced.patches.youtube.layout.hide.loadmorebutton.annotations.HideLoadMoreButtonCompatibility
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("Hide load more button")
@Description("Hides the button under videos that loads similar videos.")
@DependsOn([HideLoadMoreButtonResourcePatch::class])
@HideLoadMoreButtonCompatibility
class HideLoadMoreButtonPatch : BytecodePatch(listOf(HideLoadMoreButtonFingerprint)) {
    override fun execute(context: BytecodeContext) {
        HideLoadMoreButtonFingerprint.result?.let {
            it.mutableMethod.apply {
                val moveRegisterIndex = it.scanResult.patternScanResult!!.endIndex
                val viewRegister = getInstruction<OneRegisterInstruction>(moveRegisterIndex).registerA

                val insertIndex = moveRegisterIndex + 1
                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, " +
                            "$INTEGRATIONS_CLASS_DESCRIPTOR->hideLoadMoreButton(Landroid/view/View;)V"
                )
            }
        } ?: throw HideLoadMoreButtonFingerprint.exception
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/HideLoadMoreButtonPatch;"
    }
}
