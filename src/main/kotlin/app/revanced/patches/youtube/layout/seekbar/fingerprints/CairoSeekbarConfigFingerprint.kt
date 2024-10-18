package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patches.youtube.layout.seekbar.fingerprints.CairoSeekbarConfigFingerprint.CAIRO_SEEKBAR_FEATURE_FLAG
import app.revanced.util.patch.LiteralValueFingerprint

internal object CairoSeekbarConfigFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    parameters = listOf(),
    literalSupplier = { CAIRO_SEEKBAR_FEATURE_FLAG },
) {
    const val CAIRO_SEEKBAR_FEATURE_FLAG = 45617850L
}