package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object AboutOnClickMethodFingerprint : MethodFingerprint(
    strings = listOf(
        "//setting/about",
        "enter_from",
        "settings_page",
        "enter_settings_about"
    )
)