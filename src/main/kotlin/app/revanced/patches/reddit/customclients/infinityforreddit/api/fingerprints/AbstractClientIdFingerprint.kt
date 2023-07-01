package app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

abstract class AbstractClientIdFingerprint(classTypeSuffix: String, methodName: String) : MethodFingerprint(
    strings = listOf("NOe2iKrPPzwscA"),
    customFingerprint = custom@{ methodDef, classDef ->
        if (!classDef.type.endsWith(classTypeSuffix)) return@custom false

        methodDef.name == methodName
    }
)