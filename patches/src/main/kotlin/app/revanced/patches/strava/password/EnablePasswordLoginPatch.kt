package app.revanced.patches.strava.password

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val enablePasswordLoginPatch = bytecodePatch(
    name = "Enable password login",
    description = "Re-enables password login after having used an OTP code.",
) {
    compatibleWith("com.strava")

    execute {
        fun Fingerprint.returnTrue() = method.returnEarly(true)

        logInGetUsePasswordFingerprint.returnTrue()
        emailChangeGetUsePasswordFingerprint.returnTrue()
    }
}
