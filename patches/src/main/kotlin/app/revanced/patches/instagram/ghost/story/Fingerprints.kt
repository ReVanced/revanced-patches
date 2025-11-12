package app.revanced.patches.instagram.ghost.story

import app.revanced.patcher.fingerprint

internal val setMediaSeenHashmapFingerprint = fingerprint {
    parameters()
    returns("V")
    strings("media/seen/")
}
