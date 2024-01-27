package app.revanced.patches.photomath.misc.unlock.bookpoint.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object IsBookpointEnabledFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    strings = listOf(
        "NoGeoData",
        "NoCountryInGeo",
        "RemoteConfig",
        "GeoRCMismatch"
    )
)