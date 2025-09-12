package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.fingerprint

internal val setSeekBarShowTypeFingerprint by fingerprint {
    strings("seekbar show type change, change to:")
}

internal val shouldShowSeekBarFingerprint by fingerprint {
    strings("can not show seekbar, state: 1, not in resume")
}
