package app.revanced.patches.music.audio

import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.data.implementation.toMethodWalker
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

private val packageMetadata = listOf(
    PackageMetadata(
        "com.google.android.apps.youtube.music",
        listOf("5.03.50")
    )
)

private val patchMetadata = PatchMetadata(
    "codecs-unlock",
    "Audio codecs unlock patch",
    "Enables more audio codecs. Usually results in better audio quality but may depend on song and device.",
    packageMetadata,
    "0.0.1"
)

class CodecsUnlockPatch : BytecodePatch(
    patchMetadata,
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "codec-lock-method",
                MethodMetadata(
                    "Labwj;",
                    "a",
                ),
                PatternScanMethod.Fuzzy(2),// FIXME: Test this threshold and find the best value.
                packageMetadata,
                "Required signature for ${patchMetadata.name}. Discovered in version 5.03.50.",
                "0.0.1"
            ),
            "L",
            AccessFlags.PUBLIC or AccessFlags.STATIC,
            listOf("L", "L", "L", "L"),
            listOf(
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_DIRECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.SGET,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.INVOKE_INTERFACE,
                Opcode.INVOKE_DIRECT,
                Opcode.RETURN_OBJECT
            )
        ),
        MethodSignature(
            MethodSignatureMetadata(
                "all-codecs-reference-method",
                MethodMetadata(
                    "Laari;",
                    "b",
                ),
                PatternScanMethod.Fuzzy(2),// FIXME: Test this threshold and find the best value.
                packageMetadata,
                "Required signature for ${patchMetadata.name}. Discovered in version 5.03.50.",
                "0.0.1"
            ),
            "J",
            AccessFlags.PUBLIC or AccessFlags.FINAL,
            listOf("L"),
            listOf(
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.CONST_4,
                Opcode.IF_EQZ,
                Opcode.IGET_BOOLEAN,
                Opcode.IF_NEZ,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.IPUT_BOOLEAN,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.GOTO,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.IGET_BOOLEAN,
                Opcode.IF_NEZ,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.IPUT_BOOLEAN,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.GOTO,
                Opcode.MOVE_EXCEPTION,
                Opcode.INVOKE_SUPER,
                Opcode.MOVE_RESULT_WIDE,
                Opcode.RETURN_WIDE
            ),
            listOf("itag")
        )
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        var result = signatures.first().result!!

        val implementation = result.method.implementation!!

        val instructionIndex = result.scanData.startIndex

        result = signatures.last().result!!
        val codecMethod = data
            .toMethodWalker(result.immutableMethod)
            .walk(result.scanData.startIndex)
            .getMethod()

        implementation.replaceInstruction(
            instructionIndex,
            "invoke-static {}, ${codecMethod.definingClass}->${codecMethod.name}()Ljava/util/Set;".toInstruction()
        )

        return PatchResultSuccess()
    }
}
