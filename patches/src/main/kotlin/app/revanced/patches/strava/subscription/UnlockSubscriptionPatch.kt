package app.revanced.patches.strava.subscription

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Unlock subscription features` by creatingBytecodePatch(
    description = "Unlocks \"Routes\", \"Matched Runs\" and \"Segment Efforts\".",
) {
    compatibleWith("com.strava")

    apply {
        getSubscribedMethod.returnEarly(true)
    }
}
