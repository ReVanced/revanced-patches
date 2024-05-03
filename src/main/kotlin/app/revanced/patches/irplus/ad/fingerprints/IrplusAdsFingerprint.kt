package app.revanced.patches.irplus.ad.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val irplusAdsFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC,AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("L", "Z")
    strings("TAGGED")
}