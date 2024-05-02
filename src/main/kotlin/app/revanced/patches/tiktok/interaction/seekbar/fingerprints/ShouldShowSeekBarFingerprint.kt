package app.revanced.patches.tiktok.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val shouldShowSeekBarFingerprint = methodFingerprint {
    strings("can not show seekbar, state: 1, not in resume")
}
