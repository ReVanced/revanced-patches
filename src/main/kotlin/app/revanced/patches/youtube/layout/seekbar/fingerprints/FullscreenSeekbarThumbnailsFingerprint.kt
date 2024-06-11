package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val fullscreenSeekbarThumbnailsFingerprint = methodFingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    literal { 45398577 }
}
