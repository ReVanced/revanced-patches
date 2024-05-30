package app.revanced.patches.youtube.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val googlePlayUtilityFingerprint = methodFingerprint {
    returns("I")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("L", "I")
    strings(
        "This should never happen.",
        "MetadataValueReader",
        "com.google.android.gms",
    )
}
