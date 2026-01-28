package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableLoginRequirementPatch = bytecodePatch("Disable login requirement") {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    apply {
        listOf(
            mandatoryLoginServiceMethod,
            mandatoryLoginService2Method,
        ).forEach { method -> method.returnEarly() }
    }
}
