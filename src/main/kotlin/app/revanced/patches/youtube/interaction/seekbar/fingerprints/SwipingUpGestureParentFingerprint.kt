package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.util.patch.LiteralValueFingerprint

internal object SwipingUpGestureParentFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    parameters = listOf(),
    literalSupplier = { 45379021 }
)