package app.revanced.patches.gpsstatus2.ads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.gpsstatus2.ads.fingerprints.PlaceAdFingerprint
import app.revanced.util.exception

@Patch(
    name = "Disable Ads",
    compatiblePackages = [CompatiblePackage("com.eclipsim.gpsstatus2")]
)
@Suppress("unused")
object DisableAdsPatch : BytecodePatch(
    setOf(PlaceAdFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        PlaceAdFingerprint.result?.let { result ->
            result.mutableMethod.removeInstructions(
                result.scanResult.patternScanResult!!.endIndex,
                1
            )
        } ?: throw PlaceAdFingerprint.exception
    }
}