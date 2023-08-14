package app.revanced.patches.music.misc.androidauto.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object CheckCertificateFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("X509", "Failed to get certificate.")
)