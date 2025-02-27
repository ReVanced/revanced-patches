package app.revanced.patches.nunl.firebase

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val spoofCertificatePatch = bytecodePatch(
    name = "Spoof certificate",
    description = "Spoofs the X-Android-Cert header to allow push messages.",
) {
    compatibleWith("nl.sanomamedia.android.nu")

    execute {
        getFingerprintHashForPackageFingerprints.forEach { fingerprintBuilder ->
            val fingerprint by fingerprintBuilder

            fingerprint.method.addInstructions(
                0,
                """
                    const-string v0, "eae41fc018df2731a9b6ae1ac327da44a288667b"
                    return-object v0
                """,
            )
        }
    }
}
