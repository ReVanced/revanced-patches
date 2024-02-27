package app.revanced.patches.youtube.player.captionsbutton.fingerprints

import app.revanced.util.fingerprint.LiteralValueFingerprint

/**
 * Added in YouTube v18.31.40
 *
 * When this value is TRUE, litho subtitle button is used.
 * In this case, the empty area remains, so set this value to FALSE.
 */
object LithoSubtitleButtonConfigFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    literalSupplier = { 45421555 }
)