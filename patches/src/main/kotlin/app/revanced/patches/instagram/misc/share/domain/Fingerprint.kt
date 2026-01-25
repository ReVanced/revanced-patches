package app.revanced.patches.instagram.misc.share.domain

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.getCustomShareDomainMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
    name("getCustomShareDomain")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
}
