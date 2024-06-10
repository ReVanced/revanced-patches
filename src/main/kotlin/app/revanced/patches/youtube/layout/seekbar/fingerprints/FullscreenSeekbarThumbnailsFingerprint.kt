package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val fullscreenSeekbarThumbnailsFingerprint = methodFingerprint(
    literal { 45398577 },
) {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
}
