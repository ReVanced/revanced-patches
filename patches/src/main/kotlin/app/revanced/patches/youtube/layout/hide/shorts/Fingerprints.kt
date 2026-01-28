package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val shortsBottomBarContainerMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/View;", "Landroid/os/Bundle;")
    instructions(
        "r_pfvc"(),
        ResourceType.ID("bottom_bar_container"),
        method("getHeight"),
        Opcode.MOVE_RESULT(),
    )
}

/**
 * 19.41 to 20.44.
 */

context(_: BytecodePatchContext)
internal fun ClassDef.getRenderBottomNavigationBarMethodMatch() = firstMutableMethodDeclaratively {
    returnType("V")
    parameterTypes("Ljava/lang/String;")
    instructions(
        Opcode.IGET_OBJECT(),
        after(Opcode.MONITOR_ENTER()),
        after(Opcode.IGET_OBJECT()),
        after(Opcode.IF_EQZ()),
        after(Opcode.INVOKE_INTERFACE()),
        Opcode.MONITOR_EXIT(),
        after(Opcode.RETURN_VOID()),
        after(Opcode.MOVE_EXCEPTION()),
        after(Opcode.MONITOR_EXIT()),
        after(Opcode.THROW()),
    )
}

/**
 * Less than 19.41.
 */
internal val BytecodePatchContext.legacyRenderBottomNavigationBarLegacyParentMethod by gettingFirstMethodDeclaratively {
    parameterTypes(
        "I",
        "I",
        "L",
        "L",
        "J",
        "L",
    )
    instructions(
        "aa"(),
    )
}

/**
 * Identical to [legacyRenderBottomNavigationBarLegacyParentMethod]
 * except this has an extra parameter.
 */
internal val BytecodePatchContext.renderBottomNavigationBarLegacy1941ParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes(
        "I",
        "I",
        "L", // ReelWatchEndpointOuterClass
        "L",
        "J",
        "Ljava/lang/String;",
        "L",
    )
    instructions(
        "aa"(),
    )
}

internal val BytecodePatchContext.renderBottomNavigationBarParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("[Ljava/lang/Class;")
    parameterTypes(
        "Ljava/lang/Class;",
        "Ljava/lang/Object;",
        "I",
    )
    instructions(
        "RPCAC"(),
    )
}

internal val setPivotBarVisibilityMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Z")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
    )
}

internal val BytecodePatchContext.setPivotBarVisibilityParentMethod by gettingFirstMethodDeclaratively {
    parameterTypes("Z")
    instructions(
        "FEnotifications_inbox"(),
    )
}

internal val BytecodePatchContext.shortsExperimentalPlayerFeatureFlagMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45677719L(),
    )
}

internal val BytecodePatchContext.renderNextUIFeatureFlagMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45649743L(),
    )
}
