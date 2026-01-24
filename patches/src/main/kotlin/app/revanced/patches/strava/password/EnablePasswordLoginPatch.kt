package app.revanced.patches.strava.password

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Enable password login` by creatingBytecodePatch(
    description = "Re-enables password login after having used an OTP code.",
) {
    compatibleWith("com.strava")

    apply {
        logInGetUsePasswordMethod.returnEarly(true)
        emailChangeGetUsePasswordMethod.returnEarly(true)
    }
}
