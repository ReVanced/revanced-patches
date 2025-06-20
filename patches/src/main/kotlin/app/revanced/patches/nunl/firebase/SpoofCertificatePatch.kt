package app.revanced.patches.nunl.firebase

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val spoofCertificatePatch = bytecodePatch(
    name = "Spoof certificate",
    description = "Spoofs the X-Android-Cert header to allow push messages.",
) {
    compatibleWith("nl.sanomamedia.android.nu")

    execute {
        getFingerprintHashForPackageFingerprints.forEach { fingerprint ->
            fingerprint.method.returnEarly("eae41fc018df2731a9b6ae1ac327da44a288667b")
        }
    }
}
