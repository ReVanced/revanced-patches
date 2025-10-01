
package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.fingerprint

internal val clearNotificationReceiverFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "onReceive" &&
            classDef.type == "Lcom/instagram/notifications/push/ClearNotificationReceiver;"
    }
    strings("NOTIFICATION_DISMISSED")
}
