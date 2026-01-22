package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.setSeekBarShowTypeMethod by gettingFirstMutableMethodDeclaratively(
    "seekbar show type change, change to:"
)

internal val BytecodePatchContext.shouldShowSeekBarMethod by gettingFirstMutableMethodDeclaratively(
    "can not show seekbar, state: 1, not in resume"
)