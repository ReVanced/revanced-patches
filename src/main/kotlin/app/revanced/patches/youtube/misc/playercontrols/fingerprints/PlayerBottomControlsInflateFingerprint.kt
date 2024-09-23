package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PlayerBottomControlsInflateFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.SYNTHETIC,
    returnType = "L",
    parameters = listOf(),
    literalSupplier = { PlayerControlsResourcePatch.bottomUiContainerResourceId }
)