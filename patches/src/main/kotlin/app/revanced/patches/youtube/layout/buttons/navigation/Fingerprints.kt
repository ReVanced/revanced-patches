package app.revanced.patches.youtube.layout.buttons.navigation

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal const val ANDROID_AUTOMOTIVE_STRING = "Android Automotive"

internal val addCreateButtonViewFingerprint = fingerprint {
    strings("Android Wear", ANDROID_AUTOMOTIVE_STRING)
}

internal val createPivotBarFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters(
        "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;",
        "Landroid/widget/TextView;",
        "Ljava/lang/CharSequence;",
    )
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
}

internal const val TRANSLUCENT_NAVIGATION_STATUS_BAR_FEATURE_FLAG = 45400535L

internal val translucentNavigationStatusBarFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal { TRANSLUCENT_NAVIGATION_STATUS_BAR_FEATURE_FLAG }
}

internal const val TRANSLUCENT_NAVIGATION_BUTTONS_FEATURE_FLAG = 45630927L

internal val translucentNavigationButtonsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    literal { TRANSLUCENT_NAVIGATION_BUTTONS_FEATURE_FLAG }
}

/**
 * The device on screen back/home/recent buttons.
 */
internal const val TRANSLUCENT_NAVIGATION_BUTTONS_SYSTEM_FEATURE_FLAG = 45632194L

internal val translucentNavigationButtonsSystemFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal { TRANSLUCENT_NAVIGATION_BUTTONS_SYSTEM_FEATURE_FLAG }
}