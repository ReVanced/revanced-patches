package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.downloadAudioMethod by gettingFirstMutableMethodDeclaratively(
    "/DASH_audio.mp4",
    "/audio"
)
