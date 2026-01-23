package app.revanced.patches.lightroom.misc.bypassVersionCheck

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Disable version check` by creatingBytecodePatch(
    description = "Disables the server-side version check that prevents the app from starting.",
) {
    compatibleWith("com.adobe.lrmobile"("9.3.0"))

    apply {
        refreshRemoteConfigurationMethod.apply {
            val igetIndex = refreshRemoteConfigurationMethod.patternMatch!!.endIndex // TODO

            // This value represents the server command to clear all version restrictions.
            val statusForceReset = "-0x2"
            replaceInstruction(igetIndex, "const/4 v1, $statusForceReset")
        }
    }
}
