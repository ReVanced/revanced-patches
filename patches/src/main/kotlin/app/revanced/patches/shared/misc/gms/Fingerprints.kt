package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.googlePlayUtilityMethod by gettingFirstMethodDeclarativelyOrNull(
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

context(_: BytecodePatchContext)
internal fun ClassDef.getGmsCoreVendorGroupIdMethod() = firstMethodDeclaratively {
    name("getGmsCoreVendorGroupId")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
}

context(_: BytecodePatchContext)
internal fun ClassDef.getOriginalPackageNameExtensionMethod() = firstMethodDeclaratively {
    name("getOriginalPackageName")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
}
