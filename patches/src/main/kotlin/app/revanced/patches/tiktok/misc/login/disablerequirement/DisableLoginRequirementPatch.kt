package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Disable login requirement` by creatingBytecodePatch {
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
