package app.revanced.patches.photomath.detection.deviceid

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.photomath.detection.deviceid.fingerprints.GetDeviceIdFingerprint
import app.revanced.patches.photomath.detection.signature.SignatureDetectionPatch
import kotlin.random.Random

@Patch(
    name = "Spoof device ID",
    description = "Spoofs device ID to mitigate manual bans by developers.",
    dependencies = [SignatureDetectionPatch::class],
    compatiblePackages = [CompatiblePackage("com.microblink.photomath")]
)
@Suppress("unused")
object SpoofDeviceIdPatch : BytecodePatch(
    setOf(GetDeviceIdFingerprint)
){
    override fun execute(context: BytecodeContext) = GetDeviceIdFingerprint.result?.mutableMethod?.replaceInstructions(
        0,
        """
            const-string v0, "${Random.nextLong().toString(16)}"
            return-object v0
        """
    ) ?: throw GetDeviceIdFingerprint.exception
}