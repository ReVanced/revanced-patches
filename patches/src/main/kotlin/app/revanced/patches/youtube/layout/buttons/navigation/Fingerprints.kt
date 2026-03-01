package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

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


internal val BytecodePatchContext.pivotBarStyleMethodMatch by composingFirstMethod {
    definingClass("/PivotBar;")
    returnType("V")
    parameterTypes("L")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.XOR_INT_2ADDR
    )
}

internal val BytecodePatchContext.pivotBarChangedMethodMatch by composingFirstMethod {
    name("onConfigurationChanged")
    definingClass("/PivotBar;")
    returnType("V")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT
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

internal val BytecodePatchContext.setWordmarkHeaderMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/widget/ImageView;")
    instructions(
        ResourceType.ATTR("ytPremiumWordmarkHeader"),
        ResourceType.ATTR("ytWordmarkHeader")
    )
}

internal val BytecodePatchContext.wideSearchbarLayoutMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes("L", "L")
    instructions(ResourceType.LAYOUT("action_bar_ringo"))
}
