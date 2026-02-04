package app.revanced.patches.shared.misc.extension

import app.revanced.patcher.*
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.getPatchesReleaseVersionMethod by gettingFirstMethodDeclaratively {
    name("getPatchesReleaseVersion")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
}
