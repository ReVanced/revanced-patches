package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SettingsEntryInfoFingerprint : MethodFingerprint(
    strings = listOf(
        "ExposeItem(title=",
        ", icon="
    )
)