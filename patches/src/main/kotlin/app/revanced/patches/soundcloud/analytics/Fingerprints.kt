package app.revanced.patches.soundcloud.analytics

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val createTrackingApiFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("L")
    custom { methodDef, _ ->
        methodDef.name == "create"
    }
    strings("backend", "boogaloo")
}
