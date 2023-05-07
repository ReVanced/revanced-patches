package app.revanced.patches.youtube.layout.hide.loadmorebutton.bytecode.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.loadmorebutton.bytecode.fingerprints.HideLoadMoreButtonFingerprint
import app.revanced.patches.youtube.layout.hide.loadmorebutton.resource.patch.HideLoadMoreButtonResourcePatch
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@Name("hide-load-more-button")
@Description("Hides the button under videos that loads similar videos.")
@DependsOn([HideLoadMoreButtonResourcePatch::class])
@Version("0.0.1")
class HideLoadMoreButtonPatch : BytecodePatch(listOf(HideLoadMoreButtonFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        HideLoadMoreButtonFingerprint.result?.let {
            it.mutableMethod.apply {
                val moveRegisterIndex = it.scanResult.patternScanResult!!.endIndex
                val viewRegister = instruction<OneRegisterInstruction>(moveRegisterIndex).registerA

                val insertIndex = moveRegisterIndex + 1
                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, " +
                            "$INTEGRATIONS_CLASS_DESCRIPTOR->hideLoadMoreButton(Landroid/view/View;)V"
                )
            }
        } ?: return HideLoadMoreButtonFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/HideLoadMoreButtonPatch;"
    }
}
