package app.revanced.patches.youtube.video.hdr

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val hdrCapabilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    strings(
        "av1_profile_main_10_hdr_10_plus_supported",
        "video/av01"
    )
}
