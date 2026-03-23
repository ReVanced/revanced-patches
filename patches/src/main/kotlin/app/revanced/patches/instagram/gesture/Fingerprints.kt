package app.revanced.patches.instagram.gesture

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.doubleTapToLikeMethod by gettingFirstMethodDeclaratively("NUX_TYPE_DOUBLE_TAP_TO_LIKE")
