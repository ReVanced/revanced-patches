package app.revanced.patches.shared.misc.debugging

import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.experimentalFeatureFlagParentMethod by gettingFirstImmutableMethodDeclaratively(
    "Unable to parse proto typed experiment flag: "
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("L")
    parameterTypes("L", "J", "[B")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalBooleanFeatureFlagMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes("L", "J", "Z")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalDoubleFeatureFlagMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("D")
    parameterTypes("J", "D")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalLongFeatureFlagMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("J")
    parameterTypes("J", "J")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalStringFeatureFlagMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes("J", "Ljava/lang/String;")
}
