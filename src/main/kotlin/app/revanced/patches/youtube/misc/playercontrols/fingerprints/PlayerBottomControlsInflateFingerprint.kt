package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint

internal object PlayerBottomControlsInflateFingerprint : LiteralValueFingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf(),
    literalSupplier = { PlayerControlsResourcePatch.bottomUiContainerResourceId }
)