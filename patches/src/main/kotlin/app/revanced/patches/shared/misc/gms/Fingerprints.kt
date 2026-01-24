package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.googlePlayUtilityMethod by gettingFirstMethodDeclaratively(
    "This should never happen.",
    "MetadataValueReader",
    "com.google.android.gms",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("I")
    parameterTypes("L", "I")
}

internal val BytecodePatchContext.serviceCheckMethod by gettingFirstMethodDeclaratively(
    "Google Play Services not available",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("L", "I")
}

internal val BytecodePatchContext.gmsCoreSupportMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
    custom { method, classDef ->
        method.name == "getGmsCoreVendorGroupId" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val BytecodePatchContext.originalPackageNameExtensionMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
    custom { methodDef, classDef ->
        methodDef.name == "getOriginalPackageName" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
