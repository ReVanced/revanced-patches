package app.revanced.patches.shared.misc.debugging

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMethodDeclaratively
import app.revanced.patcher.ClassDefMethodMatching.firstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.experimentalFeatureFlagParentMethod by gettingFirstMethodDeclaratively(
    "Unable to parse proto typed experiment flag: "
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("L")
    parameterTypes("L", "J", "[B")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalBooleanFeatureFlagMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes("L", "J", "Z")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalDoubleFeatureFlagMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("D")
    parameterTypes("J", "D")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalLongFeatureFlagMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("J")
    parameterTypes("J", "J")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalStringFeatureFlagMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes("J", "Ljava/lang/String;")
}
