package app.revanced.patches.pandora.misc

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val enableUnlimitedSkipsPatch = bytecodePatch(
    name = "Enable unlimited skips",
) {
    compatibleWith("com.pandora.android")

    execute {
        skipLimitBehaviorFingerprint.method.addInstructions(0,
            """
                const-string v0, "unlimited"
                return-object v0
            """.trimIndent())
    }
}
