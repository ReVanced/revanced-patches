package app.revanced.patches.shared.misc.settings

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.extension.EXTENSION_CLASS_DESCRIPTOR
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.themeLightColorResourceNameMethod by gettingFirstMethodDeclaratively {
    name("getThemeLightColorResourceName")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
}

internal val BytecodePatchContext.themeDarkColorResourceNameMethod by gettingFirstMethodDeclaratively {
    name("getThemeDarkColorResourceName")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
}
