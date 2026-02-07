package app.revanced.patches.shared.misc.settings

import app.revanced.patcher.accessFlags
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

context(_: BytecodePatchContext)
internal fun ClassDef.getThemeLightColorResourceNameMethod() = firstMethodDeclaratively {
    name("getThemeLightColorResourceName")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
}

context(_: BytecodePatchContext)
internal fun ClassDef.getThemeDarkColorResourceNameMethod() = firstMethodDeclaratively {
    name("getThemeDarkColorResourceName")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
}
