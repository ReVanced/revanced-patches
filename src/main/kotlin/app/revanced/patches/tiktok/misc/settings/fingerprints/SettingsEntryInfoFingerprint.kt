package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val settingsEntryInfoFingerprint = methodFingerprint {
    strings(
        "ExposeItem(title=",
        ", icon=",
    )
}
