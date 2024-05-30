package app.revanced.patches.youtube.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val serviceCheckFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("L", "I")
    strings("Google Play Services not available", "GooglePlayServices not available due to error ")
}
