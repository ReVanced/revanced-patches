package app.revanced.patches.strava.subscription

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.strava.subscription.fingerprints.GetSubscribedFingerprint

@Patch(
    name = "Unlock subscription features",
    description = "Unlocks \"Matched Runs\" and \"Segment Efforts\".",
    compatiblePackages = [CompatiblePackage("com.strava", ["320.12"])]
)
@Suppress("unused")
object UnlockSubscriptionPatch : BytecodePatch(setOf(GetSubscribedFingerprint)) {
    override fun execute(context: BytecodeContext) = GetSubscribedFingerprint.result?.let { result ->
        val isSubscribedIndex = result.scanResult.patternScanResult!!.startIndex
        result.mutableMethod.replaceInstruction(isSubscribedIndex, "const/4 v0, 0x1")
    } ?: throw GetSubscribedFingerprint.exception
}