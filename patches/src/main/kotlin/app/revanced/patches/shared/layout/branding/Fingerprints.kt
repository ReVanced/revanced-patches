package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.numberOfPresetAppNamesExtensionMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("I")
    parameterTypes()
    custom { method, classDef ->
        method.name == "numberOfPresetAppNames" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

// A much simpler fingerprint exists that can set the small icon (contains string "414843287017"),
// but that has limited usage and this fingerprint allows changing any part of the notification.
internal val BytecodePatchContext.notificationMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("L")
    strings("key_action_priority")
}
