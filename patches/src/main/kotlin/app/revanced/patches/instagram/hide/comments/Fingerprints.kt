package app.revanced.patches.instagram.hide.comments

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.commentButtonMethod by gettingFirstMethodDeclaratively("feed_comment_button")
