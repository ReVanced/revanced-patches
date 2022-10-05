package app.revanced.patches.music.audio.codecs.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.music.audio.codecs.annotations.CodecsUnlockCompatibility
import app.revanced.patches.music.audio.codecs.fingerprints.AllCodecsReferenceFingerprint
import app.revanced.patches.music.audio.codecs.fingerprints.CodecsLockFingerprint
import org.jf.dexlib2.Opcode

@Patch
@Name("codecs-unlock")
@Description("Adds more audio codec options. The new audio codecs usually result in better audio quality.")
@CodecsUnlockCompatibility
@Version("0.0.1")
class CodecsUnlockPatch : BytecodePatch(
    listOf(
        CodecsLockFingerprint, AllCodecsReferenceFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val codecsLockResult = CodecsLockFingerprint.result!!

        val implementation = codecsLockResult.mutableMethod.implementation!!

        val scanResultStartIndex = codecsLockResult.scanResult.patternScanResult!!.startIndex
        val instructionIndex = scanResultStartIndex +
                if (implementation.instructions[scanResultStartIndex - 1].opcode == Opcode.CHECK_CAST) {
                    // for 5.16.xx and lower
                    -3
                } else {
                    // since 5.17.xx
                    -2
                }

        val allCodecsResult = AllCodecsReferenceFingerprint.result!!
        val allCodecsMethod =
            context.toMethodWalker(allCodecsResult.method)
                .nextMethod(allCodecsResult.scanResult.patternScanResult!!.startIndex)
                .getMethod()

        implementation.replaceInstruction(
            instructionIndex,
            "invoke-static {}, ${allCodecsMethod.definingClass}->${allCodecsMethod.name}()Ljava/util/Set;".toInstruction()
        )

        return PatchResultSuccess()
    }
}
