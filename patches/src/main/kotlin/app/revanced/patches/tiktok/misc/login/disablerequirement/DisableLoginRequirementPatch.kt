package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableLoginRequirementPatch = bytecodePatch(
    name = "Disable login requirement",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    execute {
        listOf(
            mandatoryLoginServiceFingerprint.method,
            mandatoryLoginService2Fingerprint.method,
        ).forEach { method -> method.returnEarly(false) }
    }
}
