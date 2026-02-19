package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.setSeekBarShowTypeMethod by gettingFirstMethodDeclaratively(
    "seekbar show type change, change to:"
)

internal val BytecodePatchContext.shouldShowSeekBarMethod by gettingFirstMethodDeclaratively(
    "can not show seekbar, state: 1, not in resume"
)