package app.revanced.patches.youtube.layout.castbutton.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
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
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.youtube.layout.castbutton.annotations.CastButtonCompatibility
import app.revanced.patches.youtube.layout.castbutton.signatures.CastButtonSignature
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Patch
@Dependencies(dependencies = [IntegrationsPatch::class])
@Name("disable-cast-button")
@Description("Patch to remove the cast button.")
@CastButtonCompatibility
@Version("0.0.1")
class CastButtonRemoverPatch : BytecodePatch(
    listOf(
        CastButtonSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = signatures.first().result!!
        val implementation = result.method.implementation!!

        implementation.addInstruction(
            0,
	      "invoke-static {p1}, Lfi/razerman/youtube/XGlobals;->getCastButtonOverrideV2(I)I".toInstruction()
        )

	  implementation.addInstruction(
            1,
	      "move-result p1".toInstruction()
        )
        return PatchResultSuccess()
    }
}