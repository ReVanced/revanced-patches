package app.revanced.patches.youtube.misc.audiofocus

import app.revanced.patcher.fingerprint

internal val audioFocusChangeListenerFingerprint = fingerprint {
    strings(
        "AudioFocus DUCK",
        "AudioFocus loss; Will lower volume",
    )
}

internal val audioFocusRequestBuilderFingerprint = fingerprint {
    strings("Can't build an AudioFocusRequestCompat instance without a listener")
}
