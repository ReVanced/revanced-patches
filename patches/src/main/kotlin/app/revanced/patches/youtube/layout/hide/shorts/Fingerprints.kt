package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.shortsBottomBarContainerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/View;", "Landroid/os/Bundle;")
    instructions(
        addString("r_pfvc"),
        ResourceType.ID("bottom_bar_container"),
        methodCall(name = "getHeight"),
        opcode(Opcode.MOVE_RESULT),
    )
}

/**
 * 19.41 to 20.44.
 */
internal val BytecodePatchContext.renderBottomNavigationBarMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes("Ljava/lang/String;")
    instructions(
        opcode(Opcode.IGET_OBJECT, MatchFirst()),
        opcode(Opcode.MONITOR_ENTER, MatchAfterImmediately()),
        opcode(Opcode.IGET_OBJECT, MatchAfterImmediately()),
        opcode(Opcode.IF_EQZ, MatchAfterImmediately()),
        opcode(Opcode.INVOKE_INTERFACE, MatchAfterImmediately()),

        opcode(Opcode.MONITOR_EXIT),
        opcode(Opcode.RETURN_VOID, MatchAfterImmediately()),
        opcode(Opcode.MOVE_EXCEPTION, MatchAfterImmediately()),
        opcode(Opcode.MONITOR_EXIT, MatchAfterImmediately()),
        opcode(Opcode.THROW, MatchAfterImmediately()),
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
        addString("aa"),
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
        addString("aa"),
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
        addString("RPCAC"),
    )
}

internal val BytecodePatchContext.setPivotBarVisibilityMethod by gettingFirstMethodDeclaratively {
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
        addString("FEnotifications_inbox"),
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
