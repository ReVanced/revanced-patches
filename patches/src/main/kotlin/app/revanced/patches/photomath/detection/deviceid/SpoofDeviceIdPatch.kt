package app.revanced.patches.photomath.detection.deviceid

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import kotlin.random.Random

@Suppress("unused")
val getDeviceIdPatch = bytecodePatch(
    name = "Spoof device ID",
    description = "Spoofs device ID to mitigate manual bans by developers.",
) {
    dependsOn(signatureDetectionPatch)

    compatibleWith("com.microblink.photomath"("8.37.0"))

    execute {
        getDeviceIdFingerprint.method().replaceInstructions(
            0,
            """
                const-string v0, "${Random.nextLong().toString(16)}"
                return-object v0
            """,
        )
    }
}
