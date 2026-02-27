package app.revanced.patches.samsung.radio.misc.fix.crash

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.permissionRequestListMethod by gettingFirstMethodDeclaratively(
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.READ_MEDIA_AUDIO",
    "android.permission.RECORD_AUDIO"
) {
    name("<clinit>")
}
