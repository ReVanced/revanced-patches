package app.revanced.patches.photomath.misc.unlock.bookpoint

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val isBookpointEnabledFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    strings(
        "NoGeoData",
        "NoCountryInGeo",
        "RemoteConfig",
        "GeoRCMismatch"
    )
}