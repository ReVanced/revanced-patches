package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.componentContextParserMethod by gettingFirstImmutableMethodDeclaratively {
    returnType("L")
    instructions(
        "Failed to parse Element proto."(),
        "Cannot read theme key from model."()
    )
}

context(_: BytecodePatchContext)
internal fun ClassDef.getTreeNodeResultListMethod() = firstMethodDeclaratively  {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("Ljava/util/List;")
    instructions(allOf(Opcode.INVOKE_STATIC(), method("nCopies")))
}

internal val BytecodePatchContext.shortsBottomBarContainerMethodMatch by composingFirstMethod {
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


context(_: BytecodePatchContext)
internal fun ClassDef.getRenderBottomNavigationBarMethodMatch() = firstMethodDeclaratively {
    returnType("V")
    parameterTypes("Ljava/lang/String;")
    instructions(
        Opcode.IGET_OBJECT(),
        after(Opcode.MONITOR_ENTER()),
        after(Opcode.IGET_OBJECT()),
        after(Opcode.IF_EQZ()),
        after(Opcode.INVOKE_INTERFACE()),
        Opcode.MONITOR_EXIT(),
        Opcode.RETURN_VOID(),
        Opcode.MOVE_EXCEPTION(),
        Opcode.MONITOR_EXIT(),
        Opcode.THROW()
    )
}

/**
 * Less than 19.41.
 */
internal val BytecodePatchContext.legacyRenderBottomNavigationBarLegacyParentMethod by gettingFirstImmutableMethodDeclaratively {
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
 * 19.41 - 20.44
 */
internal val BytecodePatchContext.renderBottomNavigationBarLegacy1941ParentMethod by gettingFirstImmutableMethodDeclaratively {
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

/**
 * 20.45+
 */
internal val BytecodePatchContext.renderBottomNavigationBarParentMethod by gettingFirstImmutableMethodDeclaratively {
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

internal val ClassDef.setPivotBarVisibilityMethodMatch by ClassDefComposing.composingFirstMethod {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Z")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
    )
}

internal val BytecodePatchContext.setPivotBarVisibilityParentMethod by gettingFirstImmutableMethodDeclaratively {
    parameterTypes("Z")
    instructions(
        "FEnotifications_inbox"(),
    )
}

internal val BytecodePatchContext.shortsExperimentalPlayerFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45677719L(),
    )
}

internal val BytecodePatchContext.renderNextUIFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45649743L(),
    )
}
