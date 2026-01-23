package app.revanced.patches.lightroom.misc.version

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableVersionCheckPatch = bytecodePatch(
    name = "Disable version check",
    description = "Disables the server-side version check that prevents the app from starting.",
) {
    compatibleWith("com.adobe.lrmobile"("9.3.0"))

    apply {
        val igetIndex = refreshRemoteConfigurationMethodMatch.indices.last()

        // This value represents the server command to clear all version restrictions.
        val statusForceReset = "-0x2"
        refreshRemoteConfigurationMethodMatch.method.replaceInstruction(igetIndex, "const/4 v1, $statusForceReset")
    }
}
