package app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object GetClientIdFingerprint : MethodFingerprint(
    customFingerprint = custom@{ methodDef, classDef ->
        if (!classDef.endsWith("Credentials;")) return@custom false

        methodDef.name == "getClientId"
    }
)