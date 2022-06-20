package app.revanced.patches.music.layout.tastebuilder.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.music.layout.tastebuilder.annotations.RemoveTasteBuilderCompatibility
import app.revanced.patches.music.layout.tastebuilder.signatures.TasteBuilderConstructorSignature
import org.jf.dexlib2.iface.instruction.formats.Instruction22c

@Patch
@Name("tasteBuilder-remover")
@Description("Removes the \"Tell us which artists you like\" card from the Home screen. The same functionality can be triggered from the settings anyway.")
@RemoveTasteBuilderCompatibility
@Version("0.0.1")
class RemoveTasteBuilderPatch : BytecodePatch(
    listOf(
        TasteBuilderConstructorSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = TasteBuilderConstructorSignature.result!!
        val implementation = result.method.implementation!!

        val insertIndex = result.scanResult.endIndex - 8

        val register = (implementation.instructions[insertIndex] as Instruction22c).registerA

        result.method.addInstructions(
            insertIndex, """
                const/16 v1, 0x8
                invoke-virtual {v${register}, v1}, Landroid/view/View;->setVisibility(I)V
            """
        )

        return PatchResultSuccess()
    }
}
