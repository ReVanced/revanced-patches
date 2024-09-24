package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves using the class found in [SwipingUpGestureParentFingerprint].
 */
internal object ShowSwipingUpGuideFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.FINAL.value,
    returnType = "Z",
    parameters = emptyList(),
    literalSupplier = { 1L }
)