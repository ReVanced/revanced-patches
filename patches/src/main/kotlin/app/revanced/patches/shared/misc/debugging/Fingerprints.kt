package app.revanced.patches.shared.misc.debugging

import app.revanced.patcher.ClassDefComposing
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.custom
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.experimentalFeatureFlagUtilMethod by gettingFirstImmutableMethodDeclaratively(
    "Unable to parse proto typed experiment flag: "
) {
    returnType("L")
    custom {
        // 'public static' or 'public static final'
        AccessFlags.STATIC.isSet(accessFlags)
                && AccessFlags.PUBLIC.isSet(accessFlags)
                // "L", "J", "[B" or "L", "J"
                && parameters.let { (it.size == 2 || it.size == 3) && it[1].type == "J" }
    }
}

internal val ClassDef.experimentalBooleanFeatureFlagMethodMatch by ClassDefComposing.composingFirstMethod {
    returnType("Z")
    parameterTypes("L", "J", "Z")
    custom {
        // 'public static' or 'public static final'
        AccessFlags.STATIC.isSet(accessFlags) && AccessFlags.PUBLIC.isSet(accessFlags)
    }
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalDoubleFeatureFlagMethod() = firstMethodDeclaratively {
    returnType("D")
    parameterTypes("L", "J", "D")
    custom { AccessFlags.STATIC.isSet(accessFlags) }
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalLongFeatureFlagMethod() = firstMethodDeclaratively {
    returnType("J")
    parameterTypes("L", "J", "J")
    custom { AccessFlags.STATIC.isSet(accessFlags) }
}

context(_: BytecodePatchContext)
internal fun ClassDef.getExperimentalStringFeatureFlagMethod() = firstMethodDeclaratively {
    returnType("Ljava/lang/String;")
    parameterTypes("L", "J", "Ljava/lang/String;")
    custom { AccessFlags.STATIC.isSet(accessFlags) }
}
