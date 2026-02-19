package app.revanced.patches.bandcamp.limitations

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.handlePlaybackLimitsMethod by gettingFirstMethodDeclaratively(
    "track_id",
    "play_count"
)
