package app.revanced.patches.youtube.layout.createbutton.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patches.youtube.layout.createbutton.annotations.CreateButtonCompatibility
import app.revanced.patches.youtube.layout.createbutton.signatures.CreateButtonSignature
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@Dependencies(dependencies = [IntegrationsPatch::class])
@Name("disable-create-button")
@Description("Disable the create button.")
@CreateButtonCompatibility
@Version("0.0.1")
class CreateButtonRemoverPatch : BytecodePatch(
    listOf(
        CreateButtonSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = CreateButtonSignature.result!!

        // Get the required register which holds the view object we need to pass to the method hideCreateButton
        val implementation = result.method.implementation!!
        val instruction = implementation.instructions[result.scanResult.endIndex + 1]
        if (instruction.opcode != Opcode.INVOKE_STATIC) return PatchResultError("Could not find the correct register")
        val register = (instruction as Instruction35c).registerC

        // Hide the button view via proxy by passing it to the hideCreateButton method
        result.method.addInstruction(
            result.scanResult.endIndex + 1,
            "invoke-static { v$register }, Lfi/razerman/youtube/XAdRemover;->hideCreateButton(Landroid/view/View;)V"
        )

        return PatchResultSuccess()
    }
}