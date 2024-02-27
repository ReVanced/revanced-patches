package app.revanced.patches.youtube.player.captionsbutton.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.YoutubeControlsOverlaySubtitleButton
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The parameters of the method have changed in YouTube v18.31.40.
 * Therefore, this fingerprint does not check the method's parameters.
 *
 * This fingerprint is compatible from YouTube v18.25.40 to YouTube v18.45.43
 */
object YouTubeControlsOverlaySubtitleButtonFingerprint : LiteralValueFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    literalSupplier = { YoutubeControlsOverlaySubtitleButton }
)