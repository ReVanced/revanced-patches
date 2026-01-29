package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.clearNotificationReceiverMethodMatch by composingFirstMethod {
    name("onReceive")
    definingClass("Lcom/instagram/notifications/push/ClearNotificationReceiver;")
    instructions("NOTIFICATION_DISMISSED"())
}
