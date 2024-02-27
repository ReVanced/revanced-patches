package app.revanced.patches.youtube.misc.ambientmode.fingerprints

import app.revanced.util.fingerprint.LiteralValueFingerprint

object AmbientModeInFullscreenFingerprint : LiteralValueFingerprint(
    returnType = "V",
    literalSupplier = { 45389368 }
)