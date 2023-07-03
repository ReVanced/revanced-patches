package app.revanced.patches.trakt.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object RemoteUserFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("RemoteUser;")
    }
)