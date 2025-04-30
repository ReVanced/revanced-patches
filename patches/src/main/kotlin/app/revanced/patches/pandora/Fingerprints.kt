package app.revanced.patches.pandora

import app.revanced.patcher.fingerprint

internal val constructUserDataFingerprint = fingerprint {
    strings("hasAudioAds", "skipLimitBehavior")
}
