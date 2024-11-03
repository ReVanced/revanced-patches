package app.revanced.patches.instagram.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides ads in stories, discover, profile, etc. " +
        "An ad can still appear once when refreshing the home feed.",
) {
    compatibleWith("com.instagram.android")

    execute {
        adInjectorFingerprint.matchOrThrow.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
