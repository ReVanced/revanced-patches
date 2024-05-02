package app.revanced.patches.trakt.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val isVIPEPFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        if (!methodDef.definingClass.endsWith("RemoteUser;")) return@custom false

        methodDef.name == "isVIPEP"
    }
}
