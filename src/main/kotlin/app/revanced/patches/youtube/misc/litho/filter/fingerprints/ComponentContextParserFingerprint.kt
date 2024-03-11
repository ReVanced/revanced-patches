package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ComponentContextParserFingerprint : MethodFingerprint(
    strings = listOf("Component was not found %s because it was removed due to duplicate converter bindings.")
)