package app.revanced.patches.music.premium

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

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.apps.youtube.music",
        listOf("05.03.50")
    )
)

class BackgroundPlayPatch : Patch(
    PatchMetadata(
        "background-play",
        "Enable Background Playback Patch",
        "Enable playing music in the background.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "background-playback-disabler-method",
                MethodMetadata("Lafgf;", "e"),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature for the method required to be patched.",
                "0.0.1"
            ),
            "Z",
            AccessFlags.PUBLIC or AccessFlags.STATIC,
            listOf("L"),
            listOf(
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
                Opcode.IF_EQZ,
                Opcode.CONST_4,
                Opcode.RETURN,
                Opcode.RETURN,
                Opcode.RETURN
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        signatures.first().result!!.method.implementation!!.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent().toInstructions()
        )

        return PatchResultSuccess()
    }
}