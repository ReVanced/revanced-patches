package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.returnEarly

@Suppress("unused")
val bypassCertificateChecksPatch = bytecodePatch(
    name = "Bypass certificate checks",
    description = "Bypasses certificate checks which prevent YouTube Music from working on Android Auto.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52"
        )
    )

    execute {
        addResources("music", "misc.androidauto.bypassCertificateChecksPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_music_bypass_certificate_checks"),
        )

        checkCertificateFingerprint.method.returnEarly(true)
    }
}
