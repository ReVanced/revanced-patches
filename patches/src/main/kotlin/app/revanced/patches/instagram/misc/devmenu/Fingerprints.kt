
package app.revanced.patches.instagram.misc.devmenu

internal val BytecodePatchContext.clearNotificationReceiverMethod by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        method.name == "onReceive" &&
            classDef.type == "Lcom/instagram/notifications/push/ClearNotificationReceiver;"
    }
    strings("NOTIFICATION_DISMISSED")
}
