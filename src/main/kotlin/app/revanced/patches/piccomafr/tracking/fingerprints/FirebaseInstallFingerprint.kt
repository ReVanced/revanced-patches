package app.revanced.patches.piccomafr.tracking.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags


internal object FirebaseInstallFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE.value,
    strings = listOf(
        "https://%s/%s/%s",
        "firebaseinstallations.googleapis.com"
    )
)