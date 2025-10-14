package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val numberOfPresetAppNamesExtensionFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("I")
    parameters()
    custom { method, classDef ->
        method.name == "numberOfPresetAppNames" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

// A much simpler fingerprint exists that can set the small icon (contains string "414843287017"),
// but that has limited usage and this fingerprint allows changing any part of the notification.
internal val notificationFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L")
    strings("key_action_priority")
}
