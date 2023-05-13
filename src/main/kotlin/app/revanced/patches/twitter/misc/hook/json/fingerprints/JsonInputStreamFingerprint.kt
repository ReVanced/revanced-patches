package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object JsonInputStreamFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        if (methodDef.parameterTypes.size == 0) false
        else methodDef.parameterTypes.first() == "Ljava/io/InputStream;"
    }
)