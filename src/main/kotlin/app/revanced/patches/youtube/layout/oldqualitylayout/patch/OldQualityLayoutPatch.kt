package app.revanced.patches.youtube.layout.oldqualitylayout.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.oldqualitylayout.annotations.OldQualityLayoutCompatibility
import app.revanced.patches.youtube.layout.oldqualitylayout.fingerprints.QualityMenuViewInflateFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch
@Dependencies([IntegrationsPatch::class])
@Name("old-quality-layout")
@Description("Enables the original quality flyout menu.")
@OldQualityLayoutCompatibility
@Version("0.0.1")
class OldQualityLayoutPatch : BytecodePatch(
    listOf(QualityMenuViewInflateFingerprint)
) {
    override fun execute(data: BytecodeData): PatchResult {
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
            "invoke-static { v$onItemClickViewRegister }, Lapp/revanced/integrations/patches/OldQualityLayoutPatch;->showOldQualityMenu(Landroid/widget/ListView;)V"
        )

        return PatchResultSuccess()
    }
}
