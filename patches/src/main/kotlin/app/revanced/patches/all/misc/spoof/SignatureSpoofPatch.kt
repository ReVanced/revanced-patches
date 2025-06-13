package app.revanced.patches.all.misc.spoof

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.util.getNode
import com.android.apksig.ApkVerifier
import com.android.apksig.apk.ApkFormatException
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.attribute.BasicFileAttributes
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*
import kotlin.io.path.Path

val signatureSpoofPatch = resourcePatch(
        name = "Spoof app signature",
        description = "Spoofs the app signature via the \"fake-signature\" meta key. " +
            "This patch only works with patched device roms.",
        use = false,
    ) {
    val signature by stringOption(
        key = "spoofedAppSignature",
        title = "Signature",
        validator = { signature ->
            optionToSignature(signature) != null
        },
        description = "The hex-encoded signature or path to an apk file with the desired signature",
        required = true,
    )
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getNode("manifest") as Element

            val fakeSignaturePermission = document.createElement("uses-permission")
            fakeSignaturePermission.setAttribute("android:name", "android.permission.FAKE_PACKAGE_SIGNATURE")
            manifest.appendChild(fakeSignaturePermission)

            val application = document.getNode("application") ?: {
                val child = document.createElement("application")
                manifest.appendChild(child)
                child
            } as Element;

            val fakeSignatureMetadata = document.createElement("meta-data")
            fakeSignatureMetadata.setAttribute("android:name", "fake-signature")
            fakeSignatureMetadata.setAttribute("android:value", optionToSignature(signature))
            application.appendChild(fakeSignatureMetadata)
        }
    }
}

internal fun optionToSignature(signature: String?): String? {
    if (signature == null) {
        return null;
    }
    try {
        // TODO: Replace with signature.hexToByteArray when stable in kotlin
        val signatureBytes = HexFormat.of()
            .parseHex(signature)
        val factory = CertificateFactory.getInstance("X.509")
        factory.generateCertificate(ByteArrayInputStream(signatureBytes))
        return signature;
    } catch (_: IllegalArgumentException) {
    } catch (_: CertificateException) {
    }
    try {
        val signaturePath = Path(signature)
        if (!Files.readAttributes(signaturePath, BasicFileAttributes::class.java).isRegularFile) {
            return null;
        }
        val verifier = ApkVerifier.Builder(signaturePath.toFile())
            .build()

        val result = verifier.verify()
        if (result.isVerifiedUsingV3Scheme) {
            return HexFormat.of().formatHex(result.v3SchemeSigners[0].certificate.encoded)
        } else if (result.isVerifiedUsingV2Scheme) {
            return HexFormat.of().formatHex(result.v2SchemeSigners[0].certificate.encoded)
        } else if (result.isVerifiedUsingV1Scheme) {
            return HexFormat.of().formatHex(result.v1SchemeSigners[0].certificate.encoded)
        }

        return null;
    } catch (_: IOException) {
    } catch (_: InvalidPathException) {
    } catch (_: ApkFormatException) {
    } catch (_: NoSuchAlgorithmException) {
    } catch (_: IllegalArgumentException) {}
    return null;
}