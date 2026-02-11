package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

context(_: BytecodePatchContext)
internal fun ClassDef.getNumberOfPresetAppNamesExtensionMethod() = firstMethodDeclaratively {
    name("numberOfPresetAppNames")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("I")
    parameterTypes()
}


context(_: BytecodePatchContext)
internal fun ClassDef.getUserProvidedCustomNameExtensionMethod() = firstMethodDeclaratively {
    name("userProvidedCustomName")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
}

context(_: BytecodePatchContext)
internal fun ClassDef.getUserProvidedCustomIconExtensionMethod() = firstMethodDeclaratively {
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
