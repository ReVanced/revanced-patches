package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

lateinit var lithoColorOverrideHook: (targetMethodClass: String, targetMethodName: String) -> Unit
    private set

val lithoColorHookPatch = bytecodePatch(
    description = "Adds a hook to set color of Litho components.",
) {
    val lithoThemeMatch by lithoThemeFingerprint()

    execute {
        var insertionIndex = lithoThemeMatch.patternMatch!!.endIndex - 1

        lithoColorOverrideHook = { targetMethodClass, targetMethodName ->
            lithoThemeMatch.mutableMethod.addInstructions(
                insertionIndex,
                """
                    invoke-static { p1 }, $targetMethodClass->$targetMethodName(I)I
                    move-result p1
                """,
            )
            insertionIndex += 2
        }
    }
}
