package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ComponentContextParserFingerprint : MethodFingerprint(
    strings = listOf(
        "Element missing type extension",
        "Component was not found %s because it was removed due to duplicate converter bindings."
    )
)