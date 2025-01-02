package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.LastInstructionFilter
import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.MethodFilter
import app.revanced.patcher.OpcodeFilter
import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val addCreateButtonViewFingerprint = fingerprint {
    strings("Android Automotive", "Android Wear")
}

internal val createPivotBarFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters(
        "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;",
        "Landroid/widget/TextView;",
        "Ljava/lang/CharSequence;",
    )
    instructions(
        MethodFilter(definingClass = "Landroid/widget/TextView;", methodName = "setText"),
        LastInstructionFilter(OpcodeFilter(Opcode.RETURN_VOID))
    )
}

internal val translucentNavigationStatusBarFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        LiteralFilter(45400535L)
    )
}

/**
 * YouTube nav buttons.
 */
internal val translucentNavigationButtonsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        LiteralFilter(45630927L)
    )
}

/**
 * Device on screen back/home/recent buttons.
 */
internal val translucentNavigationButtonsSystemFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        LiteralFilter(45632194L)
    )
}