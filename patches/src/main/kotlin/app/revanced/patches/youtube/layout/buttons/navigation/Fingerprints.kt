package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.addString
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val addCreateButtonViewFingerprint = fingerprint {
    instructions(
        addString("Android Wear"),
        opcode(Opcode.IF_EQZ),
        addString("Android Automotive", location = MatchAfterImmediately()),
    )
}

internal val createPivotBarFingerprint = fingerprint {
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

internal val animatedNavigationTabsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45680008L(),
    )
}

internal val translucentNavigationStatusBarFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45400535L(), // Translucent status bar feature flag.
    )
}

/**
 * YouTube nav buttons.
 */
internal val translucentNavigationButtonsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        45630927L(), // Translucent navigation bar buttons feature flag.
    )
}

/**
 * Device on screen back/home/recent buttons.
 */
internal val translucentNavigationButtonsSystemFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45632194L(), // Translucent system buttons feature flag.
    )
}
