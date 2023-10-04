package app.revanced.patches.spotify.lite.ondemand

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.spotify.lite.ondemand.fingerprints.OnDemandFingerprint

@Patch(
    name = "Enable on demand",
    description = "Enables listening to songs on-demand, allowing to play any song from playlists, albums or artists without limitations. This does not remove ads.",
    compatiblePackages = [CompatiblePackage("com.spotify.lite")]
)
@Suppress("unused")
object OnDemandPatch : BytecodePatch(setOf(OnDemandFingerprint)) {
    override fun execute(context: BytecodeContext) {
        OnDemandFingerprint.result?.apply {
            val insertIndex = scanResult.patternScanResult!!.endIndex - 1
            // Spoof a premium account
            mutableMethod.addInstruction(insertIndex, "const/4 v0, 0x2")
        } ?: throw OnDemandFingerprint.exception
    }
}
