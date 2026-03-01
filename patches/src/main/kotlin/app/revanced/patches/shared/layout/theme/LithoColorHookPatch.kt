package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

lateinit var lithoColorOverrideHook: (targetMethodClass: String, targetMethodName: String) -> Unit
    private set

val lithoColorHookPatch = bytecodePatch(
    description = "Adds a hook to set color of Litho components.",
) {

    apply {
        var insertionIndex = lithoOnBoundsChangeMethodMatch[-1] - 1

        lithoColorOverrideHook = { targetMethodClass, targetMethodName ->
            lithoOnBoundsChangeMethodMatch.method.addInstructions(
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
