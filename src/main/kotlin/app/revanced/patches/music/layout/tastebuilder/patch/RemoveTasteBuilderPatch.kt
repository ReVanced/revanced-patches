package app.revanced.patches.music.layout.tastebuilder.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.layout.tastebuilder.annotations.RemoveTasteBuilderCompatibility
import app.revanced.patches.music.layout.tastebuilder.fingerprints.TasteBuilderConstructorFingerprint
import org.jf.dexlib2.iface.instruction.formats.Instruction22c

@Patch
@Name("tasteBuilder-remover")
@Description("Removes the \"Tell us which artists you like\" card from the home screen.")
@RemoveTasteBuilderCompatibility
@Version("0.0.1")
class RemoveTasteBuilderPatch : BytecodePatch(
    listOf(
        TasteBuilderConstructorFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = TasteBuilderConstructorFingerprint.result!!
        val method = result.mutableMethod

        val insertIndex = result.scanResult.patternScanResult!!.endIndex - 8
        val register = (method.implementation!!.instructions[insertIndex] as Instruction22c).registerA
        method.addInstructions(
            insertIndex, """
                const/16 v1, 0x8
                invoke-virtual {v${register}, v1}, Landroid/view/View;->setVisibility(I)V
            """
        )

        return PatchResultSuccess()
    }
}
