package app.revanced.patches.music.audio.codecs.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.data.impl.toMethodWalker
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.music.audio.codecs.annotations.CodecsUnlockCompatibility
import app.revanced.patches.music.audio.codecs.fingerprints.AllCodecsReferenceFingerprint
import app.revanced.patches.music.audio.codecs.fingerprints.CodecsLockFingerprint

@Patch
@Name("codecs-unlock")
@Description("Enables more audio codecs. Usually results in better audio quality but may depend on song and device.")
@CodecsUnlockCompatibility
@Version("0.0.1")
class CodecsUnlockPatch : BytecodePatch(
    listOf(
        CodecsLockFingerprint, AllCodecsReferenceFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        var result = CodecsLockFingerprint.result!!

        val implementation = result.mutableMethod.implementation!!

        val instructionIndex = result.patternScanResult!!.startIndex

        result = AllCodecsReferenceFingerprint.result!!
        val codecMethod =
            data.toMethodWalker(result.method).nextMethod(result.patternScanResult!!.startIndex).getMethod()

        implementation.replaceInstruction(
            instructionIndex,
            "invoke-static {}, ${codecMethod.definingClass}->${codecMethod.name}()Ljava/util/Set;".toInstruction()
        )

        return PatchResultSuccess()
    }
}
