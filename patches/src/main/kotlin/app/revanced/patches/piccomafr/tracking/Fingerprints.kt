package app.revanced.patches.piccomafr.tracking

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val appMeasurementFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    strings("config/app/", "Fetching remote configuration")
}

internal val facebookSDKFingerprint by fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    strings("instagram.com", "facebook.com")
}

internal val firebaseInstallFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    strings(
        "https://%s/%s/%s",
        "firebaseinstallations.googleapis.com",
    )
}
