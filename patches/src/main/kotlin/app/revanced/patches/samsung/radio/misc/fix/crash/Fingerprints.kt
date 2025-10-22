@file:Suppress("unused")

package app.revanced.patches.samsung.radio.misc.fix.crash

import app.revanced.patcher.fingerprint
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.fromMethodReference
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val permissionRequestListFingerprint = fingerprint {
    strings(
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.READ_MEDIA_AUDIO",
        "android.permission.RECORD_AUDIO"
    )
    custom { method, _ ->  method.name == "<clinit>" }
}
