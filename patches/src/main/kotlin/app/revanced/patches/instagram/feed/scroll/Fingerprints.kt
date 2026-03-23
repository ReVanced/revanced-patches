package app.revanced.patches.instagram.feed.scroll

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.scrollToTopMethod by gettingFirstMethodDeclaratively("feed/scroll_to_top")
