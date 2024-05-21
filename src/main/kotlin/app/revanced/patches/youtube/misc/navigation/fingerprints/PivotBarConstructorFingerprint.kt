package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val pivotBarConstructorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    strings("com.google.android.apps.youtube.app.endpoint.flags")
}
