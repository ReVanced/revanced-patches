package app.revanced.patches.photomath.misc.unlock.bookpoint.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val isBookpointEnabledFingerprint = methodFingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    strings(
        "NoGeoData",
        "NoCountryInGeo",
        "RemoteConfig",
        "GeoRCMismatch"
    )
}