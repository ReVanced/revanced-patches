package app.revanced.patches.music.misc.androidauto.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.misc.androidauto.patch.BypassCertificateChecksPatch


object CheckCertificateFingerprint : MethodFingerprint(
    "Z",
    strings = listOf("No match") // Unique in combination with boolean return type
)