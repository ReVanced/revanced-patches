package app.revanced.patches.messenger.navbar

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction

@Suppress("unused")
val removeMetaAITabPatch = bytecodePatch(
    name = "Remove Meta AI tab",
    description = "Removes the 'Meta AI' tab from the navbar.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        createTabConfigurationFingerprint.method.replaceInstruction(
            createTabConfigurationFingerprint.patternMatch!!.startIndex + 1,
            "const/4 v2, 0x0"
        )
    }
}
