package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceLiteralFilter.Companion.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val legacyRenderBottomNavigationBarParentFingerprint by fingerprint {
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

internal val shortsBottomBarContainerFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;", "Landroid/os/Bundle;")
    instructions(
        string("r_pfvc"),
        resourceLiteral("id", "bottom_bar_container"),
        methodCall(name = "getHeight"),
        opcode(Opcode.MOVE_RESULT)
    )
}

internal val createShortsButtonsFingerprint by fingerprint {
    returns("V")
    instructions(
        resourceLiteral("dimen", "reel_player_right_cell_button_height")
    )
}

internal val renderBottomNavigationBarFingerprint by fingerprint {
    returns("V")
    parameters("Ljava/lang/String;")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.MONITOR_ENTER,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_VOID,
        Opcode.MOVE_EXCEPTION,
        Opcode.MONITOR_EXIT,
        Opcode.THROW,
    )
}

/**
 * Identical to [legacyRenderBottomNavigationBarParentFingerprint]
 * except this has an extra parameter.
 */
internal val renderBottomNavigationBarParentFingerprint by fingerprint {
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
