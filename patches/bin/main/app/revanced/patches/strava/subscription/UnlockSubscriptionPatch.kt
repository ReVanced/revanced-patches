package app.revanced.patches.strava.subscription

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockSubscriptionPatch = bytecodePatch(
    name = "Unlock subscription features",
    description = "Unlocks \"Routes\", \"Matched Runs\" and \"Segment Efforts\".",
) {
    compatibleWith("com.strava")

    execute {
        getSubscribedFingerprint.method.replaceInstruction(
            getSubscribedFingerprint.patternMatch!!.startIndex,
            "const/4 v0, 0x1",
        )
    }
}
