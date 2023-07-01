package app.revanced.patches.trakt.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object IsVIPFingerprint : MethodFingerprint(
    customFingerprint = custom@{ methodDef, _ ->
        if (!methodDef.definingClass.endsWith("RemoteUser;")) return@custom false

        methodDef.name == "isVIP"
    }
)