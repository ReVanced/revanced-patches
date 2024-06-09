package app.revanced.patches.piccomafr.tracking.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val facebookSDKFingerprint = methodFingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    strings("instagram.com", "facebook.com")
}