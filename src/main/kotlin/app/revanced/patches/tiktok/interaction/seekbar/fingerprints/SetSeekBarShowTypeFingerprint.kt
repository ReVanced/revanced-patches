package app.revanced.patches.tiktok.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val setSeekBarShowTypeFingerprint = methodFingerprint {
    strings("seekbar show type change, change to:")
}
