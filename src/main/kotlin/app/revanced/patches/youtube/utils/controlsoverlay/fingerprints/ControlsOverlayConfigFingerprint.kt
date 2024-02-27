package app.revanced.patches.youtube.utils.controlsoverlay.fingerprints

import app.revanced.util.fingerprint.LiteralValueFingerprint

/**
 * Added in YouTube v18.39.41
 *
 * When this value is TRUE, new control overlay is used.
 * In this case, the associated patches no longer work, so set this value to FALSE.
 */
object ControlsOverlayConfigFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    literalSupplier = { 45427491 }
)