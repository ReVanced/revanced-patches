package app.revanced.patches.layout

import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.*
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t

private val compatiblePackages = listOf("com.google.android.youtube")

class OldQualityLayoutPatch : Patch(
    PatchMetadata(
        "old-quality-layout",
        "Old Quality Layout Patch",
        "Enable the original quality flyout menu",
        compatiblePackages,
        "1.0.0"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "old-quality-parent-method-signature",
                MethodMetadata(null, null), // unknown
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature to find a parent method required by the Old Quality Layout patch.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
            listOf("L", "L", "L", "L", "L", "L", "L"),
            listOf(
                Opcode.IPUT_OBJECT,
                Opcode.CONST,
                Opcode.CONST,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.SGET_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.SGET_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET_BOOLEAN,
                Opcode.CONST_4,
                Opcode.CONST_4,
                Opcode.CONST,
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        val result = signatures.first().result!!

        result.findParentMethod(
            MethodSignature(
                MethodSignatureMetadata(
                    "old-quality-method-signature",
                    MethodMetadata(null, null), // unknown
                    PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                    compatiblePackages,
                    "Signature to find the method required by the Old Quality Layout patch",
                    "0.0.1"
                ),
                "L",
                AccessFlags.FINAL or AccessFlags.PUBLIC,
                emptyList(),
                listOf(
                    Opcode.IGET,
                    Opcode.CONST_4,
                    Opcode.IF_NE,
                    Opcode.IGET_OBJECT,
                    Opcode.GOTO,
                    Opcode.IGET_OBJECT,
                    Opcode.RETURN_OBJECT
                )
            )
        ) ?: return PatchResultError("Method old-quality-patch-method has not been found")

        val implementation = result.method.implementation!!

        // if useOldStyleQualitySettings == true, jump over all instructions and return the field at the end
        val jmpInstruction =
            BuilderInstruction21t(Opcode.IF_NEZ, 0, implementation.instructions[5].location.labels.first())
        implementation.addInstruction(0, jmpInstruction)
        implementation.addInstructions(
            0,
            """
                invoke-static { }, Lfi/razerman/youtube/XGlobals;->useOldStyleQualitySettings()Z
                move-result v0
            """.trimIndent().toInstructions()
        )

        return PatchResultSuccess()
    }
}