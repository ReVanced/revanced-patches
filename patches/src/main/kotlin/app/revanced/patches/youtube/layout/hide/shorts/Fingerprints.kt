package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val shortsBottomBarContainerFingerprint = fingerprint {
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
internal val renderBottomNavigationBarFingerprint = fingerprint {
    returns("V")
    parameters("Ljava/lang/String;")
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
internal val legacyRenderBottomNavigationBarLegacyParentFingerprint = fingerprint {
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
internal val renderBottomNavigationBarLegacy1941ParentFingerprint = fingerprint {
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

internal val renderBottomNavigationBarParentFingerprint = fingerprint {
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

internal val setPivotBarVisibilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
    )
}

internal val setPivotBarVisibilityParentFingerprint = fingerprint {
    parameters("Z")
    instructions(
        string("FEnotifications_inbox")
    )
}

internal val shortsExperimentalPlayerFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45677719L)
    )
}

internal val renderNextUIFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45649743L)
    )
}
