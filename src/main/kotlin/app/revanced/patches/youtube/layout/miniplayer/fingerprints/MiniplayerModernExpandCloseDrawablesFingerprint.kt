package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.miniplayer.ytOutlinePictureInPictureWhite24
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernExpandCloseDrawablesFingerprint = methodFingerprint(
    literal { ytOutlinePictureInPictureWhite24 },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
}
