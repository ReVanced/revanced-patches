package app.revanced.patches.reddit.customclients.baconreader.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getAuthorizationUrlFingerprint = methodFingerprint {
    strings("client_id=zACVn0dSFGdWqQ")
}