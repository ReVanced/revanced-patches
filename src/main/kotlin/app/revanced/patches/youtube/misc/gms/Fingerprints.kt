package app.revanced.patches.youtube.misc.gms

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val googlePlayUtilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("I")
    parameters("L", "I")
    strings(
        "This should never happen.",
        "MetadataValueReader",
        "com.google.android.gms",
    )
}

internal val serviceCheckFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters("L", "I")
    strings("Google Play Services not available", "GooglePlayServices not available due to error ")
}
