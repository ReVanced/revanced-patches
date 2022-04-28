package app.revanced.patches.music.audio

import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.*
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstruction
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.apps.youtube.music",
        listOf("5.03.50")
    )
)

class EnableAudioOnlyPatch : Patch(
    PatchMetadata(
        "audio-only-playback-patch",
        "Audio Only Mode Patch",
        "Add the option to play music without video.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "audio-only-method-signature",
                MethodMetadata("Lgmd;", "c"),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature for the method required to be patched.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.FINAL,
            listOf("L", "Z"),
            listOf(
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQ,
                Opcode.CONST_4,
                Opcode.GOTO,
                Opcode.NOP,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.IF_EQZ,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.IF_EQZ,
                Opcode.IF_EQZ,
                Opcode.INVOKE_INTERFACE,
                Opcode.INVOKE_INTERFACE,
                Opcode.GOTO,
                Opcode.RETURN_VOID
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        val result = signatures.first().result!!.findParentMethod(
            MethodSignature(
                MethodSignatureMetadata(
                    "audio-only-enabler-method",
                    MethodMetadata("Lgmd;", "d"),
                    PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                    compatiblePackages,
                    "Signature for the method required to be patched.",
                    "0.0.1"
                ),
                "Z",
                AccessFlags.PUBLIC or AccessFlags.FINAL,
                listOf(),
                listOf(
                    Opcode.IGET_OBJECT,
                    Opcode.INVOKE_INTERFACE,
                    Opcode.MOVE_RESULT_OBJECT,
                    Opcode.IGET_OBJECT,
                    Opcode.INVOKE_INTERFACE,
                    Opcode.MOVE_RESULT_OBJECT,
                    Opcode.INVOKE_VIRTUAL,
                    Opcode.MOVE_RESULT_OBJECT,
                    Opcode.CHECK_CAST,
                    Opcode.IF_NEZ,
                    Opcode.IGET_OBJECT,
                    Opcode.INVOKE_VIRTUAL,
                    Opcode.MOVE_RESULT,
                    Opcode.GOTO,
                    Opcode.INVOKE_VIRTUAL,
                    Opcode.MOVE_RESULT,
                    Opcode.RETURN
                )
            )
        ) ?: return PatchResultError("Required method for ${metadata.shortName} not found.")

        val implementation = result.method.implementation!!
        implementation.replaceInstruction(
            implementation.instructions.count() - 1,
            "const/4 v0, 0x1".toInstruction()
        )
        implementation.addInstruction(
            "return v0".toInstruction()
        )

        return PatchResultSuccess()
    }
}