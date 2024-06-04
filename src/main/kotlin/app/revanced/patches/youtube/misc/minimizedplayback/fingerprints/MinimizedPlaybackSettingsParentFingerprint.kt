package app.revanced.patches.youtube.misc.minimizedplayback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Fingerprint for [minimizedPlaybackSettingsFingerprint].
 */
internal val minimizedPlaybackSettingsParentFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("I")
    parameters()
    strings("BiometricManager", "Failure in canAuthenticate(). FingerprintManager was null.")
}
