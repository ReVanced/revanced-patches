package app.revanced.patches.instagram.ghost.story

import app.revanced.patcher.fingerprint

internal val setMediaSeenHashmapFingerprint = fingerprint {
    strings("media/seen/")
    returns("V")
    parameters()
}
