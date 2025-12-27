package app.revanced.patches.strava.password

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val enablePasswordLoginPatch = bytecodePatch(
    name = "Enable password login",
    description = "Re-enables password login after having used an OTP code.",
) {
    compatibleWith("com.strava")

    execute {
        fun Fingerprint.loadTrueInsteadOfField() =
            method.replaceInstruction(patternMatch!!.startIndex, "const/4 v0, 0x1")

        logInGetUsePasswordFingerprint.loadTrueInsteadOfField()
        emailChangeGetUsePasswordFingerprint.loadTrueInsteadOfField()
    }
}
