package app.revanced.patches.youtube.layout

import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.metadata.PackageMetadata
import app.revanced.patcher.patch.implementation.metadata.PatchMetadata
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstruction
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction11x

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube",
        listOf("17.14.35")
    )
)

class ShortsButtonRemoverPatch : BytecodePatch(
    PatchMetadata(
        "shorts-button",
        "Shorts button patch",
        "Hide the shorts button.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "pivotbar-buttons-method-tabenum",
                MethodMetadata(null, null), // unknown
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature for the pivotbar method that creates all button views.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.FINAL,
            listOf("Z"),
            listOf(
                Opcode.CHECK_CAST,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IGET_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET,
                Opcode.INVOKE_STATIC, // SomeEnum.fromValue(tabOrdinal)
                Opcode.MOVE_RESULT_OBJECT
            )
        ),
        MethodSignature(
            MethodSignatureMetadata(
                "pivotbar-buttons-method-view",
                MethodMetadata(null, null), // unknown
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature for the pivotbar method that creates all button views.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.FINAL,
            listOf("Z"),
            listOf(
                Opcode.NEW_INSTANCE, // new StateListDrawable()
                Opcode.INVOKE_DIRECT,
                Opcode.NEW_ARRAY,
                Opcode.CONST,
                Opcode.CONST_16,
                Opcode.APUT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.MOVE,
                Opcode.MOVE_OBJECT,
                Opcode.INVOKE_VIRTUAL_RANGE, // pivotBar.getView(drawable, tabName, z, i, map, akebVar, optional)
                Opcode.MOVE_RESULT_OBJECT,
            )
        ),
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result1 = signatures.first().result!!
        val implementation1 = result1.method.implementation!!
        val moveEnumInstruction = implementation1.instructions[result1.scanData.endIndex]
        val enumRegister = (moveEnumInstruction as Instruction11x).registerA

        val result2 = signatures.last().result!!
        val implementation2 = result2.method.implementation!!
        val moveViewInstruction = implementation2.instructions[result2.scanData.endIndex]
        val viewRegister = (moveViewInstruction as Instruction11x).registerA

        // Save the tab enum in XGlobals to avoid smali/register workarounds
        implementation1.addInstruction(
            result1.scanData.endIndex + 1,
            "sput-object v$enumRegister, Lfi/razerman/youtube/XGlobals;->lastPivotTab:Ljava/lang/Enum;".toInstruction()
        )

        // Hide the button view via proxy by passing it to the hideShortsButton method
        // It only hides it if the last tab name is "TAB_SHORTS"
        implementation2.addInstruction(
            result2.scanData.endIndex + 2,
            "invoke-static { v$viewRegister }, Lfi/razerman/youtube/XAdRemover;->hideShortsButton(Landroid/view/View;)V".toInstruction()
        )

        return PatchResultSuccess()
    }
}
