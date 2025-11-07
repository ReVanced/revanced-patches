package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val shortsBottomBarContainerFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;", "Landroid/os/Bundle;")
    instructions(
        string("r_pfvc"),
        resourceLiteral(ResourceType.ID, "bottom_bar_container"),
        methodCall(name = "getHeight"),
        opcode(Opcode.MOVE_RESULT)
    )
}

/**
 * 19.41 to 20.44.
 */
internal val renderBottomNavigationBarFingerprint by fingerprint {
    returns("V")
    parameters("Ljava/lang/String;")
    instructions(
        opcode(Opcode.IGET_OBJECT, maxAfter = 0),
        opcode(Opcode.MONITOR_ENTER, maxAfter = 0),
        opcode(Opcode.IGET_OBJECT, maxAfter = 0),
        opcode(Opcode.IF_EQZ, maxAfter = 0),
        opcode(Opcode.INVOKE_INTERFACE, maxAfter = 0),

        opcode(Opcode.MONITOR_EXIT),
        opcode(Opcode.RETURN_VOID, maxAfter = 0),
        opcode(Opcode.MOVE_EXCEPTION, maxAfter = 0),
        opcode(Opcode.MONITOR_EXIT, maxAfter = 0),
        opcode(Opcode.THROW, maxAfter = 0),
    )
}

/**
 * Less than 19.41.
 */
internal val legacyRenderBottomNavigationBarLegacyParentFingerprint by fingerprint {
    parameters(
        "I",
        "I",
        "L",
        "L",
        "J",
        "L",
    )
    instructions(
        string("aa")
    )
}

/**
 * Identical to [legacyRenderBottomNavigationBarLegacyParentFingerprint]
 * except this has an extra parameter.
 */
internal val renderBottomNavigationBarLegacy1941ParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters(
        "I",
        "I",
        "L", // ReelWatchEndpointOuterClass
        "L",
        "J",
        "Ljava/lang/String;",
        "L",
    )
    instructions(
        string("aa")
    )
}

internal val renderBottomNavigationBarParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("[Ljava/lang/Class;")
    parameters(
        "Ljava/lang/Class;",
        "Ljava/lang/Object;",
        "I"
    )
    instructions(
        string("RPCAC")
    )
}

internal val setPivotBarVisibilityFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
    )
}

internal val setPivotBarVisibilityParentFingerprint by fingerprint {
    parameters("Z")
    instructions(
        string("FEnotifications_inbox")
    )
}

internal val shortsExperimentalPlayerFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45677719L)
    )
}

internal val renderNextUIFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45649743L)
    )
}
