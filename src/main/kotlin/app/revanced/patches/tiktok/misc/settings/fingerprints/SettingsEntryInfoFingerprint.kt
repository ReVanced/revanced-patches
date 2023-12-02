package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SettingsEntryInfoFingerprint : MethodFingerprint(
    strings = listOf(
        "ExposeItem(title=",
        ", icon="
    )
)