package app.revanced.patches.pandora.shared

import app.revanced.patcher.fingerprint

internal val constructUserDataFingerprint = fingerprint {
    strings("hasAudioAds", "skipLimitBehavior")
}
