package app.revanced.patches.youtube.layout.shorts.button.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.youtube.layout.shorts.button.annotations.ShortsButtonCompatibility
import app.revanced.patches.youtube.layout.shorts.button.signatures.PivotBarButtonTabenumSignature
import app.revanced.patches.youtube.layout.shorts.button.signatures.PivotBarButtonsViewSignature
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.formats.Instruction11x

@Patch(dependencies = [IntegrationsPatch::class])
@Name("shorts-button")
@Description("Hide the shorts button.")
@ShortsButtonCompatibility
@Version("0.0.1")
class ShortsButtonRemoverPatch : BytecodePatch(
    listOf(
        PivotBarButtonTabenumSignature, PivotBarButtonsViewSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result1 = signatures.first().result!!
        val implementation1 = result1.method.implementation!!
        val moveEnumInstruction = implementation1.instructions[result1.scanResult.endIndex]
        val enumRegister = (moveEnumInstruction as Instruction11x).registerA

        val result2 = signatures.last().result!!
        val implementation2 = result2.method.implementation!!
        val moveViewInstruction = implementation2.instructions[result2.scanResult.endIndex]
        val viewRegister = (moveViewInstruction as Instruction11x).registerA

        // Save the tab enum in XGlobals to avoid smali/register workarounds
        implementation1.addInstruction(
            result1.scanResult.endIndex + 1,
            "sput-object v$enumRegister, Lfi/razerman/youtube/XGlobals;->lastPivotTab:Ljava/lang/Enum;".toInstruction()
        )

        // Hide the button view via proxy by passing it to the hideShortsButton method
        // It only hides it if the last tab name is "TAB_SHORTS"
        implementation2.addInstruction(
            result2.scanResult.endIndex + 2,
            "invoke-static { v$viewRegister }, Lfi/razerman/youtube/XAdRemover;->hideShortsButton(Landroid/view/View;)V".toInstruction()
        )

        return PatchResultSuccess()
    }
}
