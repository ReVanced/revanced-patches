package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.addCreateButtonViewMethodMatch by composingFirstMethod {
    instructions(
        "Android Wear"(),
        Opcode.IF_EQZ(),
        after("Android Automotive"()),
    )
}

internal val BytecodePatchContext.createPivotBarMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes(
        "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;",
        "Landroid/widget/TextView;",
        "Ljava/lang/CharSequence;",
    )
    instructions(
        method { name == "setText" && definingClass == "Landroid/widget/TextView;" },
        Opcode.RETURN_VOID(),
    )
}

internal val BytecodePatchContext.animatedNavigationTabsFeatureFlagMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45680008L(),
    )
}

internal val BytecodePatchContext.translucentNavigationStatusBarFeatureFlagMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45400535L(), // Translucent status bar feature flag.
    )
}

/**
 * YouTube nav buttons.
 */
internal val BytecodePatchContext.translucentNavigationButtonsFeatureFlagMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        45630927L(), // Translucent navigation bar buttons feature flag.
    )
}

/**
 * Device on screen back/home/recent buttons.
 */
internal val BytecodePatchContext.translucentNavigationButtonsSystemFeatureFlagMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45632194L(), // Translucent system buttons feature flag.
    )
}
