package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.smali.asInstruction
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t

class OldQualityLayout : Patch("old-quality-restore") {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["old-quality-patch"].findParentMethod(
            MethodSignature(
                "old-quality-patch-method",
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
        ) ?: return PatchResultError("Parent method old-quality-patch-method has not been found")

        val implementation = map.method.implementation!!

        // if useOldStyleQualitySettings == true, jump over all instructions and return the field at the end
        val jmpInstruction =
            BuilderInstruction21t(Opcode.IF_NEZ, 0, implementation.instructions[5].location.labels.first())
        implementation.addInstruction(0, jmpInstruction)
        implementation.addInstruction(
            0,
            """
                invoke-static { }, Lfi/razerman/youtube/XGlobals;->useOldStyleQualitySettings()Z
                move-result v0
            """.trimIndent().asInstruction()
        )

        return PatchResultSuccess()
    }
}