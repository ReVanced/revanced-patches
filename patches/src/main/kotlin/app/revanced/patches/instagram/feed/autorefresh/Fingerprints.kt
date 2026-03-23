package app.revanced.patches.instagram.feed.autorefresh

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.pullToRefreshMethod by gettingFirstMethodDeclaratively("disable_pull_to_refresh")
