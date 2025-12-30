package app.revanced.patches.tiktok.misc.login.fixgoogle

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val fixGoogleLoginPatch = bytecodePatch(
    name = "Fix Google login",
    description = "Allows logging in with a Google account.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    apply {
        listOf(
            googleOneTapAuthAvailableFingerprint.method,
            googleAuthAvailableFingerprint.method,
        ).forEach { method ->
            method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
        }
    }
}
