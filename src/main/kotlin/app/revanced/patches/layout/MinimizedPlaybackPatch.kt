package app.revanced.patches.layout

import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.AccessFlagExtensions.Companion.or
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.*
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.asInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

private val compatiblePackages = listOf("com.google.android.youtube")

class MinimizedPlaybackPatch : Patch(
    metadata = PatchMetadata(
        "minimized-playback",
        "Minimized Playback Patch",
        "Enable minimized and background playback.",
        compatiblePackages,
        "1.0.0"
    ),
    signatures = listOf(
        MethodSignature(
            methodSignatureMetadata = MethodSignatureMetadata(
                name = "minimized-playback-manager",
                methodMetadata = MethodMetadata(null, null), // unknown
                patternScanMethod = PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages = compatiblePackages,
                description = "Signature for the method required to be patched.",
                version = "0.0.1"
            ),
            returnType = "Z",
            accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
            methodParameters = listOf("L"),
            opcodes = listOf(
                Opcode.CONST_4,
                Opcode.IF_EQZ,
                Opcode.IGET,
                Opcode.AND_INT_LIT16,
                Opcode.IF_EQZ,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET,
                Opcode.CONST,
                Opcode.IF_NE,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET,
                Opcode.IF_NE,
                Opcode.IGET_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.GOTO,
                Opcode.SGET_OBJECT,
                Opcode.GOTO,
                Opcode.CONST_4,
                Opcode.IF_EQZ,
                Opcode.IGET_BOOLEAN,
                Opcode.IF_EQ
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        // Instead of removing all instructions like Vanced,
        // we return the method at the beginning instead
        signatures.first().result!!.method.implementation!!.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
                """.trimIndent().asInstructions()
        )
        return PatchResultSuccess()
    }
}