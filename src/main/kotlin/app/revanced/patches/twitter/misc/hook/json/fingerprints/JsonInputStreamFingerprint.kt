package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object JsonInputStreamFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        if (methodDef.parameterTypes.size == 0) false
        else methodDef.parameterTypes.first() == "Ljava/io/InputStream;"
    }
)