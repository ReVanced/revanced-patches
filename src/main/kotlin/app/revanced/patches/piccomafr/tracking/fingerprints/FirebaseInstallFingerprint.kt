package app.revanced.patches.piccomafr.tracking.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val firebaseInstallFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE)
    strings(
        "https://%s/%s/%s",
        "firebaseinstallations.googleapis.com"
    )
}