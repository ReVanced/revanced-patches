package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

/**
 * Hooks most color usage of litho components.
 */
lateinit var lithoColorOverrideHook: (targetMethodClass: String, targetMethodName: String) -> Unit
    private set

val lithoColorHookPatch = bytecodePatch(
    description = "Adds a hook to set color of Litho components.",
) {

    execute {
        var insertionIndex = lithoThemeFingerprint.instructionMatches.last().index

        lithoColorOverrideHook = { targetMethodClass, targetMethodName ->
            lithoThemeFingerprint.method.addInstructions(
                insertionIndex,
                """
                    invoke-static { p1 }, $targetMethodClass->$targetMethodName(I)I
                    move-result p1
                """
            )
            insertionIndex += 2
        }
    }
}
