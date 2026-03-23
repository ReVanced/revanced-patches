package app.revanced.patches.instagram.ghost.story

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.setMediaSeenMethod by gettingFirstMethodDeclaratively("visual_media_seen")
