package app.revanced.patches.youtube.video.codecs

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val vp9CapabilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    strings(
        "vp9_supported",
        "video/x-vnd.on2.vp9"
    )
}
