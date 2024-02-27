package app.revanced.patches.youtube.general.songsearch.fingerprints

import app.revanced.util.fingerprint.LiteralValueFingerprint

/**
 * This fingerprint is compatible with YouTube v18.30.37+
 */
object VoiceSearchConfigFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    literalSupplier = { 45417109 }
)