package app.revanced.patches.trakt

import app.revanced.patcher.fingerprint

internal val isVIPEPFingerprint by fingerprint {
    custom { method, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        method.name == "isVIPEP"
    }
}

internal val isVIPFingerprint by fingerprint {
    custom { method, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        method.name == "isVIP"
    }
}

internal val remoteUserFingerprint by fingerprint {
    custom { _, classDef ->
        classDef.endsWith("RemoteUser;")
    }
}
