package app.revanced.patches.samsung.radio.misc.fix.crash

import app.revanced.patcher.fingerprint

internal val permissionRequestListFingerprint = fingerprint {
    strings(
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.READ_MEDIA_AUDIO",
        "android.permission.RECORD_AUDIO"
    )
    custom { method, _ ->  method.name == "<clinit>" }
}
