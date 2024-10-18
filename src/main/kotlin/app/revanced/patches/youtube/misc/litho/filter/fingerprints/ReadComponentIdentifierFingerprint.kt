package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

/**
 * In 19.17 and earlier, this resolves to the same method as [ComponentContextParserFingerprint].
 * In 19.18+ this resolves to a different method.
 */
internal object ReadComponentIdentifierFingerprint : MethodFingerprint(
    strings = listOf("Number of bits must be positive")
)