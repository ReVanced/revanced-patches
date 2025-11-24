package app.revanced.patches.lightroom.misc.version

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
/**
 * Disables the server-side version check that prevents the app from starting
 * if the version is considered "denylisted" or below the minimum requirement.
 */
val DisableVersionCheckPatch = bytecodePatch(
    name = "Disable version check",
) {
    compatibleWith("com.adobe.lrmobile"("9.3.0"))

    execute {
        versionCheckFingerprint.method.apply {
            val igetIndex = versionCheckFingerprint.patternMatch!!.endIndex

            // This value represents the server command to clear all version restrictions
            val STATUS_FORCE_RESET_HEX = "-0x2";
            replaceInstruction(igetIndex, "const/4 v1, $STATUS_FORCE_RESET_HEX")
        }
    }
}
