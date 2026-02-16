package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.numberOfPresetAppNamesExtensionMethod by gettingFirstMethodDeclaratively {
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    name("numberOfPresetAppNames")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("I")
    parameterTypes()
}


internal val BytecodePatchContext.userProvidedCustomNameExtensionMethod by gettingFirstMethodDeclaratively {
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    name("userProvidedCustomName")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.userProvidedCustomIconExtensionMethod by gettingFirstMethodDeclaratively {
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    name("userProvidedCustomIcon")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
}

// A much simpler method exists that can set the small icon (contains string "414843287017"),
// but that has limited usage and this one allows changing any part of the notification.
internal val BytecodePatchContext.notificationMethod by gettingFirstMethodDeclaratively(
    "key_action_priority",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("L")
}
