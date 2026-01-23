package app.revanced.patches.shared.misc.settings

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.extension.EXTENSION_CLASS_DESCRIPTOR
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.themeLightColorResourceNameMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
    custom { method, classDef ->
        method.name == "getThemeLightColorResourceName" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val BytecodePatchContext.themeDarkColorResourceNameMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
    custom { method, classDef ->
        method.name == "getThemeDarkColorResourceName" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
