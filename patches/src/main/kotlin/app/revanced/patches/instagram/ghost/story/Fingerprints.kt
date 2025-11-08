package app.revanced.patches.instagram.ghost.story

import app.revanced.patcher.fingerprint

internal val setMediaSeenHashmapFingerprint = fingerprint {
    returns("V")
    parameters()
    strings("media/seen/")
}
