package app.revanced.patches.strava.privacy

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val blockSnowplowTrackingPatch = bytecodePatch(
    name = "Block Snowplow tracking",
    description = "Blocks Snowplow analytics. See https://snowplow.io for more information.",
) {
    compatibleWith("com.strava")

    execute {
    	// Keep events list empty, otherwise sent to https://c.strava.com/com.snowplowanalytics.snowplow/tp2.
        insertEventFingerprint.method.returnEarly()
    }
}
