package app.revanced.patches.youtube.layout

import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.metadata.PackageMetadata
import app.revanced.patcher.patch.implementation.metadata.PatchMetadata
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstruction
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube",
        listOf("17.14.35")
    )
)

class CreateButtonRemoverPatch : BytecodePatch(
    PatchMetadata(
        "create-button",
        "Create button patch",
        "Disable the create button.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "create-button-method",
                MethodMetadata(null, null), // unknown
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature for the method required to be patched.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.FINAL,
            listOf("Z"),
            listOf(
                Opcode.IGET,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.NEW_INSTANCE,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.CONST,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.INVOKE_DIRECT_RANGE,
                Opcode.CONST_4,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
            )
        )
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = signatures.first().result!!

        // Get the required register which holds the view object we need to pass to the method hideCreateButton
        val implementation = result.method.implementation!!
        val instruction = implementation.instructions[result.scanData.endIndex + 1]
        if (instruction.opcode != Opcode.INVOKE_STATIC)
            return PatchResultError("Could not find the correct register")
        val register = (instruction as Instruction35c).registerC

        // Hide the button view via proxy by passing it to the hideCreateButton method
        implementation.addInstruction(
            result.scanData.endIndex + 1,
            "invoke-static { v$register }, Lfi/razerman/youtube/XAdRemover;->hideCreateButton(Landroid/view/View;)V".toInstruction()
        )

        return PatchResultSuccess()
    }
}