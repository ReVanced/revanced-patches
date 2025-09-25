package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

lateinit var lithoColorOverrideHook: (targetMethodClass: String, targetMethodName: String) -> Unit
    private set

val lithoColorHookPatch = bytecodePatch(
    description = "Adds a hook to set color of Litho components.",
) {

    execute {
        var insertionIndex = lithoOnBoundsChangeFingerprint.patternMatch!!.endIndex - 1

        lithoColorOverrideHook = { targetMethodClass, targetMethodName ->
            lithoOnBoundsChangeFingerprint.method.addInstructions(
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
