package app.revanced.patches.peacocktv.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val mediaTailerAdServiceFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/lang/Object")
    strings("Could not build MT Advertising service")
}
