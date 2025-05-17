package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val gmsCoreSupportFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    custom { method, classDef ->
        method.name == "getGmsCoreVendorGroupId"
                && classDef.endsWith("/GmsCoreSupport;")
    }
}

internal val googlePlayUtilityFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("I")
    parameters("L", "I")
    strings(
        "This should never happen.",
        "MetadataValueReader",
        "com.google.android.gms",
    )
}

internal val serviceCheckFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters("L", "I")
    strings("Google Play Services not available")
}
