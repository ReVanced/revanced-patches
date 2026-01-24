package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Bypass certificate checks` by creatingBytecodePatch(
    description = "Bypasses certificate checks which prevent YouTube Music from working on Android Auto.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52",
        ),
    )

    apply {
        checkCertificateMethod.returnEarly(true)
    }
}
