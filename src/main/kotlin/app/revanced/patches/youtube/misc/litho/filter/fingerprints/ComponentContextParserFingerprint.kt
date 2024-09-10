package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

/**
 * In 19.17 and earlier, this resolves to the same method as [ReadComponentIdentifierFingerprint].
 * In 19.18+ this resolves to a different method.
 */
internal object ComponentContextParserFingerprint : MethodFingerprint(
    strings = listOf("Component was not found %s because it was removed due to duplicate converter bindings.")
)