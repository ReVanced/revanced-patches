package app.revanced.patches.youtube.layout.tablet.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.tablet.TabletLayoutResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves using the class found in [ModernMiniPlayerViewParentFingerprint].
 */
internal object ModernMiniPlayerOverlayViewFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf(),
    literalSupplier = { TabletLayoutResourcePatch.scrimOverlay }
)