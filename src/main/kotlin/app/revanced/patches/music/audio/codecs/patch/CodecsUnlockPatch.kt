package app.revanced.patches.music.audio.codecs.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.data.implementation.toMethodWalker
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.music.audio.codecs.annotations.CodecsUnlockCompatibility
import app.revanced.patches.music.audio.codecs.signatures.AllCodecsReferenceSignature
import app.revanced.patches.music.audio.codecs.signatures.CodecsLockSignature

@Patch
@Name("codecs-unlock")
@Description("Enables more audio codecs. Usually results in better audio quality but may depend on song and device.")
@CodecsUnlockCompatibility
@Version("0.0.1")
class CodecsUnlockPatch : BytecodePatch(
    listOf(
        CodecsLockSignature, AllCodecsReferenceSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        var result = signatures.first().result!!

        val implementation = result.method.implementation!!

        val instructionIndex = result.scanData.startIndex

        result = signatures.last().result!!
        val codecMethod = data.toMethodWalker(result.immutableMethod).walk(result.scanData.startIndex).getMethod()

        implementation.replaceInstruction(
            instructionIndex,
            "invoke-static {}, ${codecMethod.definingClass}->${codecMethod.name}()Ljava/util/Set;".toInstruction()
        )

        return PatchResultSuccess()
    }
}
