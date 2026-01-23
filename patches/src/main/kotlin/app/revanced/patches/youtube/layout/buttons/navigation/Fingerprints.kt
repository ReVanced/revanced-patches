package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.addCreateButtonViewMethod by gettingFirstMethodDeclaratively {
    instructions(
        addString("Android Wear"),
        opcode(Opcode.IF_EQZ),
        addString("Android Automotive", location = MatchAfterImmediately()),
    )
}

internal val BytecodePatchContext.createPivotBarMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes(
        "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;",
        "Landroid/widget/TextView;",
        "Ljava/lang/CharSequence;",
    )
    instructions(
        methodCall(definingClass = "Landroid/widget/TextView;", name = "setText"),
        opcode(Opcode.RETURN_VOID),
    )
}

internal val BytecodePatchContext.animatedNavigationTabsFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45680008L(),
    )
}

internal val BytecodePatchContext.translucentNavigationStatusBarFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45400535L(), // Translucent status bar feature flag.
    )
}

/**
 * YouTube nav buttons.
 */
internal val BytecodePatchContext.translucentNavigationButtonsFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        45630927L(), // Translucent navigation bar buttons feature flag.
    )
}

/**
 * Device on screen back/home/recent buttons.
 */
internal val BytecodePatchContext.translucentNavigationButtonsSystemFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45632194L(), // Translucent system buttons feature flag.
    )
}
