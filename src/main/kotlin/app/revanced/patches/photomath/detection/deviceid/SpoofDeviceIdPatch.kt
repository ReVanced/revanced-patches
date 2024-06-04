package app.revanced.patches.photomath.detection.deviceid

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.photomath.detection.deviceid.fingerprints.getDeviceIdFingerprint
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import kotlin.random.Random

@Suppress("unused")
val spoofDeviceIdPatch = bytecodePatch(
    name = "Spoof device ID",
    description = "Spoofs device ID to mitigate manual bans by developers.",
    dependencies = [SignatureDetectionPatch::class],
    compatiblePackages = [CompatiblePackage("com.microblink.photomath", ["8.37.0"])]
)

@Suppress("unused")
object SpoofDeviceIdPatch : BytecodePatch(
    setOf(GetDeviceIdFingerprint)
) {
    dependsOn(signatureDetectionPatch)

    compatibleWith("com.microblink.photomath"("8.32.0"))

    val getDeviceIdResult by getDeviceIdFingerprint

    execute
    {
        getDeviceIdResult.mutableMethod.replaceInstructions(
            0,
            """
                const-string v0, "${Random.nextLong().toString(16)}"
                return-object v0
            """
        )
    }

}