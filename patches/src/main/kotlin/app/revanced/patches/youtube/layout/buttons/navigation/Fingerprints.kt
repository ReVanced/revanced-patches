package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val addCreateButtonViewFingerprint = fingerprint {
    instructions(
        string("Android Wear"),
        opcode(Opcode.IF_EQZ),
        string("Android Automotive", location = MatchAfterImmediately()),
    )
}

internal val createPivotBarFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters(
        "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;",
        "Landroid/widget/TextView;",
        "Ljava/lang/CharSequence;",
    )
    instructions(
        methodCall(definingClass = "Landroid/widget/TextView;", name = "setText"),
        opcode(Opcode.RETURN_VOID)
    )
}

internal val animatedNavigationTabsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        literal(45680008L)
    )
}

internal val translucentNavigationStatusBarFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        literal(45400535L) // Translucent status bar feature flag.
    )
}

/**
 * YouTube nav buttons.
 */
internal val translucentNavigationButtonsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        literal(45630927L) // Translucent navigation bar buttons feature flag.
    )
}

/**
 * Device on screen back/home/recent buttons.
 */
internal val translucentNavigationButtonsSystemFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        literal(45632194L) // Translucent system buttons feature flag.
    )
}
