package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.feedItemParseFromJsonMethod by gettingFirstMethodDeclaratively("feed_item_type")
