package app.revanced.patches.bandcamp.limitations

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.handlePlaybackLimitsMethod by gettingFirstMutableMethodDeclaratively(
    "track_id",
    "play_count"
)
