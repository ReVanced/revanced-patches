package app.revanced.patches.youtube.layout.hide.loadmorebutton

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.loadmorebutton.fingerprints.HideLoadMoreButtonFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide load more button",
    description = "Hides the button under videos that loads similar videos.",
    dependencies = [HideLoadMoreButtonResourcePatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object HideLoadMoreButtonPatch : BytecodePatch(
    setOf(HideLoadMoreButtonFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/HideLoadMoreButtonPatch;"

    override fun execute(context: BytecodeContext) {
        HideLoadMoreButtonFingerprint.result?.let {
            it.mutableMethod.apply {
                val moveRegisterIndex = it.scanResult.patternScanResult!!.endIndex
                val viewRegister =
                    getInstruction<OneRegisterInstruction>(moveRegisterIndex).registerA

                val insertIndex = moveRegisterIndex + 1
                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, " +
                            "$INTEGRATIONS_CLASS_DESCRIPTOR->hideLoadMoreButton(Landroid/view/View;)V"
                )
            }
        } ?: throw HideLoadMoreButtonFingerprint.exception
    }
}
