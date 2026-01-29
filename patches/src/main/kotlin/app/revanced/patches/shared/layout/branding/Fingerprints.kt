package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.numberOfPresetAppNamesExtensionMethod by gettingFirstMutableMethodDeclaratively {
    name("numberOfPresetAppNames")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("I")
    parameterTypes()
}

// A much simpler method exists that can set the small icon (contains string "414843287017"),
// but that has limited usage and this one allows changing any part of the notification.
internal val BytecodePatchContext.notificationMethod by gettingFirstMutableMethodDeclaratively(
    "key_action_priority",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("L")
}
