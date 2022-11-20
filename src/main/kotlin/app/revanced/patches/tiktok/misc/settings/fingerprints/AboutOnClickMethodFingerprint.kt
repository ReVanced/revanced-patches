package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.misc.settings.annotations.TikTokSettingsCompatibility

@Name("about-onclick-method-fingerprint")
@TikTokSettingsCompatibility
@Version("0.0.1")
object AboutOnClickMethodFingerprint : MethodFingerprint(
    strings = listOf(
        "//setting/about",
        "enter_from",
        "settings_page",
        "enter_settings_about"
    )
)