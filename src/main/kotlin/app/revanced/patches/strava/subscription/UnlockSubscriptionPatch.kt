package app.revanced.patches.strava.subscription
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockSubscriptionPatch = bytecodePatch(
    name = "Unlock subscription features",
    description = "Unlocks \"Routes\", \"Matched Runs\" and \"Segment Efforts\".",
) {
    compatibleWith("com.strava")

    val getSubscribedFingerprintResult by getSubscribedFingerprint

    execute {
        getSubscribedFingerprintResult.mutableMethod.replaceInstruction(
            getSubscribedFingerprintResult.scanResult.patternScanResult!!.startIndex,
            "const/4 v0, 0x1",
        )
    }
}
