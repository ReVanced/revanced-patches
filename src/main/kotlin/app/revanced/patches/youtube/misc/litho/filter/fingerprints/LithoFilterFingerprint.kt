package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object LithoFilterFingerprint : MethodFingerprint(
    customFingerprint = custom@{ method, classDef ->
        if (method.name != "<clinit>") return@custom false

        classDef.type.endsWith("LithoFilterPatch;")
    }
)