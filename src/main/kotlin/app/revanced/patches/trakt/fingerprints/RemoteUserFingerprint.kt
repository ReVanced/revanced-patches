package app.revanced.patches.trakt.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object RemoteUserFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("RemoteUser;")
    }
)