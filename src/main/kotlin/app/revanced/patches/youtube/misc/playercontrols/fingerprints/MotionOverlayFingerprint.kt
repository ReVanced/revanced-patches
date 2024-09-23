package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MotionOverlayFingerprint : LiteralValueFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    parameters = listOf(),
    literalSupplier = { PlayerControlsResourcePatch.fastForwardRewindHintContainer }
)