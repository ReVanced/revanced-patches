package app.revanced.patches.music.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val serviceCheckFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    strings("Google Play Services not available")
}
