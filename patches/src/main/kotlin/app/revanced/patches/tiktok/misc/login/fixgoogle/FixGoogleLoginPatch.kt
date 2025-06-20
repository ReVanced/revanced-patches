package app.revanced.patches.tiktok.misc.login.fixgoogle

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val fixGoogleLoginPatch = bytecodePatch(
    name = "Fix Google login",
    description = "Allows logging in with a Google account.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    execute {
        listOf(
            googleOneTapAuthAvailableFingerprint.method,
            googleAuthAvailableFingerprint.method,
        ).forEach { method -> method.returnEarly(false) }
    }
}
