package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.miniplayer.modernMiniplayerExpand
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernExpandButtonFingerprint = methodFingerprint(
    literal { modernMiniplayerExpand },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/widget/ImageView;")
    parameters()
}
