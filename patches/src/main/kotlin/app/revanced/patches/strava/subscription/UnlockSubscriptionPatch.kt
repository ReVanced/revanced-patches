package app.revanced.patches.strava.subscription

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val unlockSubscriptionPatch = bytecodePatch(
    name = "Unlock subscription features",
    description = "Unlocks \"Routes\", \"Matched Runs\" and \"Segment Efforts\".",
) {
    compatibleWith("com.strava")

    execute {
        getSubscribedFingerprint.method.returnEarly(true)
    }
}
