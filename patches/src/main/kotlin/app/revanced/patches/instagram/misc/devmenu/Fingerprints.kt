package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.*

internal val clearNotificationReceiverMethodMatch = firstMethodComposite {
    name("onReceive")
    definingClass("Lcom/instagram/notifications/push/ClearNotificationReceiver;")
    instructions("NOTIFICATION_DISMISSED"())
}
