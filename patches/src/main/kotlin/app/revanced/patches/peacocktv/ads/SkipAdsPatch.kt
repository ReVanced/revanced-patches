package app.revanced.patches.peacocktv.ads

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val skipAdsPatch = bytecodePatch(
    name = "Skip ads",
    description = "Automatically skips ads.",
) {
    compatibleWith("com.peacocktv.peacockandroid")

    execute {
        mediaTailerAdServiceFingerprint.method.replaceInstructions(
            0,
            """
                const/4 v0, 0
                return-object v0
            """,
        )
    }
}
