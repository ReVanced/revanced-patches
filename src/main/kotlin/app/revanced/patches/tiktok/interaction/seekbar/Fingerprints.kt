package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.fingerprint.methodFingerprint

internal val setSeekBarShowTypeFingerprint = methodFingerprint {
    strings("seekbar show type change, change to:")
}

internal val shouldShowSeekBarFingerprint = methodFingerprint {
    strings("can not show seekbar, state: 1, not in resume")
}
