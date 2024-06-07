package app.revanced.patches.piccomafr.tracking.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags


internal object FacebookSDKFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.STATIC or AccessFlags.CONSTRUCTOR,
    strings = listOf(
        "instagram.com",
        "facebook.com"
    ),
    returnType = "V"
)