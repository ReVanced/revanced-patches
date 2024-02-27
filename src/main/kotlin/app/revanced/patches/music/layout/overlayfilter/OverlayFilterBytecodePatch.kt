package app.revanced.patches.music.layout.overlayfilter

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.layout.overlayfilter.fingerprints.DesignBottomSheetDialogFingerprint
import app.revanced.patches.music.utils.integrations.Constants.GENERAL
import app.revanced.patches.music.utils.integrations.IntegrationsPatch
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    dependencies = [
        IntegrationsPatch::class,
        SharedResourceIdPatch::class
    ],
    requiresIntegrations = true
)
object OverlayFilterBytecodePatch : BytecodePatch(
    setOf(DesignBottomSheetDialogFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        DesignBottomSheetDialogFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex - 1
                val freeRegister = getInstruction<OneRegisterInstruction>(insertIndex + 1).registerA

                addInstructions(
                    insertIndex, """
                        invoke-virtual {p0}, $definingClass->getWindow()Landroid/view/Window;
                        move-result-object v$freeRegister
                        invoke-static {v$freeRegister}, $GENERAL->disableDimBehind(Landroid/view/Window;)V
                        """
                )
            }
        } ?: throw DesignBottomSheetDialogFingerprint.exception

    }
}