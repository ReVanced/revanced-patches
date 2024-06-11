package app.revanced.patches.trakt

import app.revanced.patcher.fingerprint.methodFingerprint

internal val isVIPEPFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        methodDef.name == "isVIPEP"
    }
}

internal val isVIPFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        methodDef.name == "isVIP"
    }
}

internal val remoteUserFingerprint = methodFingerprint {
    custom { _, classDef ->
        classDef.endsWith("RemoteUser;")
    }
}
