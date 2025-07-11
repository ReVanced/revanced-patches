package app.revanced.patches.all.misc.spoof

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.util.getNode
import com.android.apksig.ApkVerifier
import com.android.apksig.apk.ApkFormatException
import org.w3c.dom.Element
import java.io.File
import java.io.IOException
import java.nio.file.InvalidPathException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*

@Suppress("unused")
val enableRomSignatureSpoofing = resourcePatch(
    name = "Enable ROM signature spoofing",
    description = "Spoofs the signature via the manifest meta-data \"fake-signature\". " +
            "This patch only works with ROMs that support signature spoofing.",
    use = false,
) {
    val signatureOrPath by stringOption(
        key = "signatureOrApkFilePath",
        title = "Signature or APK file path",
        validator = validator@{ signature ->
            signature ?: return@validator false

            parseSignature(signature) != null
        },
        description = "The hex-encoded signature or path to an APK file with the desired signature.",
        required = true,
    )
    execute {
        document("AndroidManifest.xml").use { document ->
            val permission = document.createElement("uses-permission").apply {
                setAttribute("android:name", "android.permission.FAKE_PACKAGE_SIGNATURE")
            }
            val manifest = document.getNode("manifest").appendChild(permission)


            val fakeSignatureMetadata = document.createElement("meta-data").apply {
                setAttribute("android:name", "fake-signature")
                setAttribute("android:value", parseSignature(signatureOrPath!!))
            }
            document.getNode("application").appendChild(fakeSignatureMetadata)
        }
    }
}

private fun parseSignature(optionValue: String): String? {
    // Parse as a hex-encoded signature.
    try {
        // TODO: Replace with signature.hexToByteArray when stable in kotlin
        val signatureBytes = HexFormat.of().parseHex(optionValue)
        CertificateFactory.getInstance("X.509").generateCertificate(signatureBytes.inputStream())

        return optionValue
    } catch (_: IllegalArgumentException) {
    } catch (_: CertificateException) {
    }

    // Parse as a path to an APK file.
    try {
        val apkFile = File(optionValue)
        if (!apkFile.isFile) return null

        val result = ApkVerifier.Builder(apkFile).build().verify()

        val hexFormat = HexFormat.of()

        val signature = (if (result.isVerifiedUsingV3Scheme) {
            result.v3SchemeSigners[0].certificate
        } else if (result.isVerifiedUsingV2Scheme) {
            result.v2SchemeSigners[0].certificate
        } else if (result.isVerifiedUsingV1Scheme) {
            result.v1SchemeSigners[0].certificate
        } else {
            return null
        }).encoded

        return hexFormat.formatHex(signature)
    } catch (_: IOException) {
    } catch (_: InvalidPathException) {
    } catch (_: ApkFormatException) {
    } catch (_: NoSuchAlgorithmException) {
    } catch (_: IllegalArgumentException) {
    }

    return null
}