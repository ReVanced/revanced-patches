package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.clearNotificationReceiverMethod by gettingFirstMutableMethodDeclaratively(
    "NOTIFICATION_DISMISSED",
) {
    name("onReceive")
    definingClass("Lcom/instagram/notifications/push/ClearNotificationReceiver;")
}
