package app.revanced.patches.strava.privacy

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Block Snowplow tracking` by creatingBytecodePatch(
    description = "Blocks Snowplow analytics. See https://snowplow.io for more information.",
) {
    compatibleWith("com.strava")

    apply {
        // Keep events list empty, otherwise sent to https://c.strava.com/com.snowplowanalytics.snowplow/tp2.
        insertEventMethod.returnEarly()
    }
}
