package app.revanced.patches.trakt

import app.revanced.patcher.fingerprint

internal val isVIPEPFingerprint = fingerprint {
    custom { method, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        method.name == "isVIPEP"
    }
}

internal val isVIPFingerprint = fingerprint {
    custom { method, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        method.name == "isVIP"
    }
}

internal val remoteUserFingerprint = fingerprint {
    custom { _, classDef ->
        classDef.endsWith("RemoteUser;")
    }
}
