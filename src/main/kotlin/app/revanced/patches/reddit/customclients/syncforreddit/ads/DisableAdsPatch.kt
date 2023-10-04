package app.revanced.patches.reddit.customclients.syncforreddit.ads

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.ads.fingerprints.IsAdsEnabledFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.DisablePiracyDetectionPatch

@Patch(
    name = "Disable ads",
    dependencies = [DisablePiracyDetectionPatch::class],
    compatiblePackages = [CompatiblePackage("com.laurencedawson.reddit_sync")]
)
@Suppress("unused")
object DisableAdsPatch : BytecodePatch(setOf(IsAdsEnabledFingerprint)) {
    override fun execute(context: BytecodeContext) {
        IsAdsEnabledFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
                """
                const/4 v0, 0x0
                return v0
            """
            )
        } ?: throw IsAdsEnabledFingerprint.exception
    }
}