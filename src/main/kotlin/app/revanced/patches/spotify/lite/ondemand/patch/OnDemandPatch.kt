package app.revanced.patches.spotify.lite.ondemand.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.spotify.lite.ondemand.annotations.OnDemandCompatibility
import app.revanced.patches.spotify.lite.ondemand.fingerprints.OnDemandFingerprint

@Patch
@Name("enable-on-demand")
@Description("Enables listening to songs on-demand, allowing to play any song from playlists, albums or artists without limitations. This does not remove ads.")
@OnDemandCompatibility
@Version("0.0.1")
class OnDemandPatch : BytecodePatch(
    listOf(
        OnDemandFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        OnDemandFingerprint.result?.apply {
            val insertIndex = scanResult.patternScanResult!!.endIndex - 1
            // Spoof a premium account
            mutableMethod.addInstruction(insertIndex, "const/4 v0, 0x2")
        } ?: return OnDemandFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
