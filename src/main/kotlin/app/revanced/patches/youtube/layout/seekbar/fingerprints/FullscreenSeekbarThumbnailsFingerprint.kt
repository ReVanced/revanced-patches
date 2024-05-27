package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val fullscreenSeekbarThumbnailsFingerprint = literalValueFingerprint(
    literalSupplier = { 45398577 },
) {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
}
