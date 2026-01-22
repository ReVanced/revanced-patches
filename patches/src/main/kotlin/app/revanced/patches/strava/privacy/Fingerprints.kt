package app.revanced.patches.strava.privacy

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

// https://github.com/snowplow/snowplow-android-tracker/blob/2.2.0/snowplow-tracker/src/main/java/com/snowplowanalytics/snowplow/internal/emitter/storage/SQLiteEventStore.java#L130
// Not the exact same code (e.g. returns void instead of long), even though the version number matches.
internal val BytecodePatchContext.insertEventMethod by gettingFirstMutableMethodDeclaratively("Added event to database: %s")
