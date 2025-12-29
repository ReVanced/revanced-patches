package app.revanced.patches.strava.privacy

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

/**
 * The app collects user interaction events and sends them to
 * https://c.strava.com/com.snowplowanalytics.snowplow/tp2.
 * This patch prevents the insertion of events, keeping the list of emittable events empty.
 */
@Suppress("unused")
val blockSnowplowTrackingPatch = bytecodePatch(
    name = "Block Snowplow tracking",
    description = "Blocks Snowplow analytics. See https://snowplow.io for more information.",
) {
    compatibleWith("com.strava")

    execute {
        insertEventFingerprint.method.returnEarly()
    }
}
