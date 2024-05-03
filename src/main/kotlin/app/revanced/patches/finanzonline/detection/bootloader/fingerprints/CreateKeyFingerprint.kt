package app.revanced.patches.finanzonline.detection.bootloader.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Located @ at.gv.bmf.bmf2go.taxequalization.tools.utils.AttestationHelper#createKey (3.0.1)
internal val createKeyFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC.value)
    returns("Z")
    strings("attestation", "SHA-256", "random", "EC", "AndroidKeyStore")
}
