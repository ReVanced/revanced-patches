package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val experimentalFeatureFlagParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L", "J", "[B")
    strings("Unable to parse proto typed experiment flag: ")
}

internal val experimentalBooleanFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("J", "Z")
}

internal val experimentalDoubleFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("D")
    parameters("J", "D")
}

internal val experimentalLongFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("J")
    parameters("J", "J")
}

internal val experimentalStringFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters("J", "Ljava/lang/String;")
}

