package app.revanced.patches.shared.misc.debugging

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val experimentalFeatureFlagParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L", "J", "[B")
    strings("Unable to parse proto typed experiment flag: ")
}

internal val experimentalBooleanFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Z")
    parameters("L", "J", "Z")
}

internal val experimentalDoubleFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("D")
    parameters("J", "D")
}

internal val experimentalLongFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("J")
    parameters("J", "J")
}

internal val experimentalStringFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters("J", "Ljava/lang/String;")
}
