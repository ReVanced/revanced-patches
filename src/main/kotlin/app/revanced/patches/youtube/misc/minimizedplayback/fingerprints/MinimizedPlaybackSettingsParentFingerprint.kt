package app.revanced.patches.youtube.misc.minimizedplayback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

/**
 * Class fingerprint for [MinimizedPlaybackSettingsFingerprint]
 */
object MinimizedPlaybackSettingsParentFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("Landroid/content/Context;", "Landroid/support/v4/media/session/MediaSessionCompat"),
    strings = listOf("sessionToken must not be null")
)
