package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Suppress("unused")
val signatureCheckPatch = bytecodePatch(
    name = "Disable signature check",
    description = "Disables the signature check that causes any modified app to crash on startup."
) {
    compatibleWith("com.instagram.android"("378.0.0.52.68"))

    // Patching method is inspired by:
    // https://github.com/mamiiblt/instafel/blob/032c6a714a4a862462cd4bcd106083f640b13219/instafel.patcher/src/main/java/me/mamiiblt/instafel/patcher/patches/fix/FixSecureCtxCrash.java
    //
    // Logic has been adapted to rely less on garbled method names that are likely to change.
    // Instagram's code is highly obfuscated, so any comments on code flow are best guesses.
    execute {
        // Main activity insertion point from NotificationAction receiver bypasses IgSecureContext check.
        // Get the method it calls, and use it to replace the method called by the launcher.
        val safeMethod = onReceiveNotificationFingerprint.let { it ->
            navigate(it.method)
                .to(it.patternMatch!!.startIndex) // navigate into invoke-static
                .to { instr -> instr.opcode == Opcode.INVOKE_VIRTUAL } // navigate to first invoke-virtual
                .original()
        }

        launcherFingerprint.let {
            navigate(it.method)
                .to(it.patternMatch!!.startIndex) // navigate into invoke-static
                .stop() // patch this method
        }.apply {
            val targetIndex = indexOfFirstInstruction(Opcode.INVOKE_VIRTUAL)
            val origReg = getInstruction<FiveRegisterInstruction>(targetIndex).registerD

            // Only replace the method called on this class's singleton.
            replaceInstruction(
                targetIndex,
                "invoke-virtual { v$origReg }, $safeMethod"
            )
        }
    }
}
