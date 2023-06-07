package app.revanced.patches.youtube.video.oldqualitylayout.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.video.oldqualitylayout.annotations.OldQualityLayoutCompatibility
import app.revanced.patches.youtube.video.oldqualitylayout.fingerprints.QualityMenuViewInflateFingerprint
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, OldQualityLayoutResourcePatch::class])
@Name("old-quality-layout")
@Description("Enables the original video quality flyout in the video player settings.")
@OldQualityLayoutCompatibility
@Version("0.0.1")
class OldQualityLayoutPatch : BytecodePatch(listOf(QualityMenuViewInflateFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val inflateFingerprintResult = QualityMenuViewInflateFingerprint.result!!
        val method = inflateFingerprintResult.mutableMethod
        val instructions = method.implementation!!.instructions

        // at this index the listener is added to the list view
        val listenerInvokeRegister = instructions.size - 1 - 1

        // get the register which stores the quality menu list view
        val onItemClickViewRegister = (instructions[listenerInvokeRegister] as FiveRegisterInstruction).registerC

        // insert the integrations method
        method.addInstruction(
            listenerInvokeRegister, // insert the integrations instructions right before the listener
            "invoke-static { v$onItemClickViewRegister }, Lapp/revanced/integrations/patches/playback/quality/OldQualityLayoutPatch;->showOldQualityMenu(Landroid/widget/ListView;)V"
        )

        return PatchResultSuccess()
    }
}