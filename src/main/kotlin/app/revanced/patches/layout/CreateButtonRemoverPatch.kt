package app.revanced.patches.layout

import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.AccessFlagExtensions.Companion.or
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchMetadata
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.asInstruction
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

private val compatiblePackages = arrayOf("com.google.android.youtube")

class CreateButtonRemoverPatch : Patch(
    metadata = PatchMetadata(
        "create-button",
        "Create button patch",
        "Disable the create button.",
        compatiblePackages,
        "1.0.0"
    ),
    signatures = listOf(
        MethodSignature(
            methodSignatureMetadata = MethodSignatureMetadata(
                name = "create-button-method",
                methodMetadata = MethodMetadata(null, null), // unknown
                patternScanMethod = PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages = compatiblePackages,
                description = "Signature for the method required to be patched.",
                version = "0.0.1"
            ),
            returnType = "V",
            accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
            methodParameters = listOf("Z"),
            opcodes = listOf(
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
                Opcode.MOVE_OBJECT
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        val result = signatures.first().result!!

        // Hide the button view via proxy by passing it to the hideCreateButton method
        result.method.implementation!!.addInstruction(
            result.scanData.endIndex,
            "invoke-static { v2 }, Lfi/razerman/youtube/XAdRemover;->hideCreateButton(Landroid/view/View;)V".asInstruction()
        )

        return PatchResultSuccess()
    }
}