package app.revanced.patches.trakt.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val isVIPFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        methodDef.name == "isVIP"
    }
}
