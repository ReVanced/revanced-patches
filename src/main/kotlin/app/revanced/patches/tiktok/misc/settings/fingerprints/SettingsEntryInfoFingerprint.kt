package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object SettingsEntryInfoFingerprint : MethodFingerprint(
    strings = listOf(
        "ExposeItem(title=",
        ", icon="
    )
)