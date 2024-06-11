package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.miniplayer.scrimOverlay
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernOverlayViewFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    literal { scrimOverlay }
}
