package app.revanced.patches.lightroom.misc.version

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
val disableVersionCheckPatch = bytecodePatch(
    name = "Disable version check",
    description = "Disables the server-side version check that prevents the app from starting.",
) {
    compatibleWith("com.adobe.lrmobile"("9.3.0"))

    execute {
        refreshRemoteConfigurationFingerprint.method.apply {
            val igetIndex = refreshRemoteConfigurationFingerprint.patternMatch!!.endIndex

            // This value represents the server command to clear all version restrictions.
            val statusForceReset = "-0x2";
            replaceInstruction(igetIndex, "const/4 v1, $statusForceReset")
        }
    }
}
