package app.revanced.patches.trakt.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val remoteUserFingerprint = methodFingerprint {
    custom { _, classDef ->
        classDef.endsWith("RemoteUser;")
    }
}
