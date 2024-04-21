package app.revanced.patches.strava.subscription
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.strava.subscription.fingerprints.GetSubscribedFingerprint
import app.revanced.util.exception

@Patch(
    name = "Unlock subscription features",
    description = "Unlocks \"Routes\", \"Matched Runs\" and \"Segment Efforts\".",
    compatiblePackages = [CompatiblePackage("com.strava")],
)
@Suppress("unused")
object UnlockSubscriptionPatch : BytecodePatch(setOf(GetSubscribedFingerprint)) {
    override fun execute(context: BytecodeContext) = GetSubscribedFingerprint.result?.let { result ->
        val isSubscribedIndex = result.scanResult.patternScanResult!!.startIndex
        result.mutableMethod.replaceInstruction(isSubscribedIndex, "const/4 v0, 0x1")
    } ?: throw GetSubscribedFingerprint.exception
}
