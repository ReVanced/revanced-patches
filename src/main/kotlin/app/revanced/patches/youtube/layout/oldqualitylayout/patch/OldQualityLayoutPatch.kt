package app.revanced.patches.youtube.layout.oldqualitylayout.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.layout.oldqualitylayout.annotations.OldQualityLayoutCompatibility
import app.revanced.patches.youtube.layout.oldqualitylayout.signatures.OldQualityParentSignature
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t

@Patch
@Dependencies(dependencies = [IntegrationsPatch::class])
@Name("old-quality-layout")
@Description("Enable the original quality flyout menu.")
@OldQualityLayoutCompatibility
@Version("0.0.1")
class OldQualityLayoutPatch : BytecodePatch(
    listOf(
        OldQualityParentSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = OldQualityParentSignature.result!!.findParentMethod(@Name("old-quality-signature") @MatchingMethod(
            definingClass = "Libh"
        ) @FuzzyPatternScanMethod(2) @OldQualityLayoutCompatibility @Version("0.0.1") object : MethodSignature(
            "L", AccessFlags.FINAL or AccessFlags.PRIVATE, listOf("Z"), listOf(
                Opcode.CONST_4,
                Opcode.INVOKE_VIRTUAL,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.IGET_OBJECT,
                Opcode.GOTO,
                Opcode.IGET_OBJECT,
            )
        ) {}) ?: return PatchResultError("Required parent method could not be found.")

        val implementation = result.method.implementation!!

        // if useOldStyleQualitySettings == true, jump over all instructions
        val jmpInstruction = BuilderInstruction21t(
            Opcode.IF_NEZ, 0, implementation.instructions[result.scanResult.endIndex].location.labels.first()
        )
        implementation.addInstruction(5, jmpInstruction)
        implementation.addInstructions(
            0, """
                invoke-static { }, Lfi/razerman/youtube/XGlobals;->useOldStyleQualitySettings()Z
                move-result v0
            """.trimIndent().toInstructions()
        )

        return PatchResultSuccess()
    }
}