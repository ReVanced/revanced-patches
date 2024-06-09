package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val jsonInputStreamFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        if (methodDef.parameterTypes.size == 0) false
        else methodDef.parameterTypes.first() == "Ljava/io/InputStream;"
    }
}