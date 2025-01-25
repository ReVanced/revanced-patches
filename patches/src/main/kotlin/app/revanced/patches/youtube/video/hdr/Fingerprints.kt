package app.revanced.patches.youtube.video.hdr

import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal val hdrCapabilityFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    instructions(
        string("av1_profile_main_10_hdr_10_plus_supported"),
        string("video/av01"),
        methodCall(returnType = "Z", parameters = listOf("I", "Landroid/view/Display;"))
    )
}