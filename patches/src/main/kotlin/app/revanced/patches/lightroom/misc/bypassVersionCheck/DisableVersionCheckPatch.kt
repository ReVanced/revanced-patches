package app.revanced.patches.lightroom.misc.version

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
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
            val igetIndex = implementation!!.instructions.indexOfFirst {
                it.opcode == Opcode.IGET
            }

            if (igetIndex != -1) {
                addInstruction(igetIndex + 1, "const/4 v1, -0x2")
            }
        }
    }
}
