package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.containsWideLiteralInstructionValue
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val bottomNavigationBarFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;", "Landroid/os/Bundle;")
    opcodes(
        Opcode.CONST, // R.id.app_engagement_panel_wrapper
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
    )
    strings("ReelWatchPaneFragmentViewModelKey")
}

internal val createShortsButtonsFingerprint = methodFingerprint {
    returns("V")
    literal { reelPlayerRightCellButtonHeight }
}

internal val reelConstructorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(Opcode.INVOKE_VIRTUAL)
    custom { methodDef, _ ->
        // Cannot use methodFingerprint, because the resource id may not be present.
        val reelMultipleItemShelfId = reelMultipleItemShelfId
        reelMultipleItemShelfId != -1L &&
            methodDef.containsWideLiteralInstructionValue(reelMultipleItemShelfId)
    }
}

internal val renderBottomNavigationBarFingerprint = methodFingerprint {
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

internal val renderBottomNavigationBarParentFingerprint = methodFingerprint {
    parameters("I", "I", "L", "L", "J", "L")
    strings("aa")
}

internal val setPivotBarVisibilityFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
    )
}

internal val setPivotBarVisibilityParentFingerprint = methodFingerprint {
    parameters("Z")
    strings("FEnotifications_inbox")
}
