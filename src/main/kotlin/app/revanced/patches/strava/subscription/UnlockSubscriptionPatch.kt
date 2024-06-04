package app.revanced.patches.strava.subscription
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.strava.subscription.fingerprints.getSubscribedFingerprint

@Suppress("unused")
val unlockSubscriptionPatch = bytecodePatch(
    name = "Unlock subscription features",
    description = "Unlocks \"Routes\", \"Matched Runs\" and \"Segment Efforts\".",
) {
    compatibleWith("com.strava")

    val getSubscribedResult by getSubscribedFingerprint

    execute {
        getSubscribedResult.mutableMethod.replaceInstruction(
            getSubscribedResult.scanResult.patternScanResult!!.startIndex,
            "const/4 v0, 0x1",
        )
    }
}
