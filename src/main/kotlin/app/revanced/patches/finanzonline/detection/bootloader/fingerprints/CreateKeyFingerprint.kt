package app.revanced.patches.finanzonline.detection.bootloader.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Located @ at.gv.bmf.bmf2go.taxequalization.tools.utils.AttestationHelper#createKey (3.0.1)
object CreateKeyFingerprint : MethodFingerprint(
    "Z",
    accessFlags = AccessFlags.PUBLIC.value,
    strings = listOf("attestation", "SHA-256", "random", "EC", "AndroidKeyStore")
)
