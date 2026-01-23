package app.revanced.patches.tiktok.misc.login.fixgoogle

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Fix Google login` by creatingBytecodePatch(
    description = "Allows logging in with a Google account.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    apply {
        listOf(
            googleOneTapAuthAvailableMethod,
            googleAuthAvailableMethod,
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
