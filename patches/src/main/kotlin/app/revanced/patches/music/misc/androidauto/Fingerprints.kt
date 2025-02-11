package app.revanced.patches.music.misc.androidauto

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val checkCertificateFingerprint = fingerprint {
    returns("Z")
    parameters("Ljava/lang/String;")
    strings(
        "X509",
        "Failed to get certificate" // Partial String match.
    )
}