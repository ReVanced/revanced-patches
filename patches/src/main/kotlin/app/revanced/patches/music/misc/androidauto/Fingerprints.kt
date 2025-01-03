package app.revanced.patches.music.misc.androidauto

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val checkCertificateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("Ljava/lang/String;")
    strings("X509", "Failed to get certificate.")
}