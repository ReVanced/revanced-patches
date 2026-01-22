package app.revanced.patches.tiktok.interaction.downloads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.aclCommonShareMethod by gettingFirstMutableMethodDeclaratively {
    name("getCode")
    definingClass("/ACLCommonShare;"::endsWith)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("I")
}
internal val BytecodePatchContext.aclCommonShare2Method by gettingFirstMutableMethodDeclaratively {
    name("getShowType")
    definingClass("/ACLCommonShare;"::endsWith)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("I")
}
internal val BytecodePatchContext.aclCommonShare3Method by gettingFirstMutableMethodDeclaratively {
    name("getTranscode")
    definingClass("/ACLCommonShare;"::endsWith)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("I")
}
internal val BytecodePatchContext.downloadUriMethod by gettingFirstMutableMethodDeclaratively(
    "/",
    "/Camera",
    "/Camera/",
    "video/mp4"
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Landroid/net/Uri;")
    parameterTypes(
        "Landroid/content/Context;",
        "Ljava/lang/String;"
    )
}