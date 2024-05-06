package app.revanced.patches.irplus.ad.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val irplusAdsFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC,AccessFlags.CONSTRUCTOR)
    parameters("L", "Z")
    strings("TAGGED")
}