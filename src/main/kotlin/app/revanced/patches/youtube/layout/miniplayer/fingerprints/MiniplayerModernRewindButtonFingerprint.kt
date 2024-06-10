package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.miniplayer.modernMiniplayerRewindButton
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernRewindButtonFingerprint = methodFingerprint(
    literal { modernMiniplayerRewindButton },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/widget/ImageView;")
    parameters()
}
