package app.revanced.patches.trakt.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val remoteUserFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("RemoteUser;")
    }
}
