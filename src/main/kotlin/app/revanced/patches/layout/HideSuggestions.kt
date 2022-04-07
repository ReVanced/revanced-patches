package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.smali.asInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

class HideSuggestions : Patch("hide-suggestions") {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["hide-suggestions-patch"].findParentMethod(
            MethodSignature(
                "hide-suggestions-method",
                "V",
                AccessFlags.PUBLIC or AccessFlags.PUBLIC,
                setOf("Z"),
                arrayOf(
                    Opcode.IPUT_BOOLEAN,
                    Opcode.IGET_OBJECT,
                    Opcode.IPUT_BOOLEAN,
                    Opcode.INVOKE_VIRTUAL,
                    Opcode.RETURN_VOID
                )
            )
        ) ?: return PatchResultError("Parent method hide-suggestions-method has not been found")

        // Proxy the first parameter by passing it to the RemoveSuggestions method
        map.resolveAndGetMethod().implementation!!.addInstructions(
            0,
            """
                invoke-static { p1 }, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v0
                invoke-static { v0 }, Lfi/razerman/youtube/XAdRemover;->RemoveSuggestions(Ljava/lang/Boolean;)Ljava/lang/Boolean;
                move-result-object v0
                invoke-virtual { v0 }, Ljava/lang/Boolean;->booleanValue()Z
                move-result v0
            """.trimIndent().asInstructions()
        )
        return PatchResultSuccess()
    }
}