package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.patches.youtube.layout.buttons.navigation.navigationButtonsPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val actionBarSearchResultsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    instructions(
        resourceLiteral(ResourceType.LAYOUT, "action_bar_search_results_view_mic"),
        methodCall(name = "setLayoutDirection")
    )
}

internal val toolbarLayoutFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral(ResourceType.ID, "toolbar_container"),
        checkCast("Lcom/google/android/apps/youtube/app/ui/actionbar/MainCollapsingToolbarLayout;")
    )
}

/**
 * Matches to https://android.googlesource.com/platform/frameworks/support/+/9eee6ba/v7/appcompat/src/android/support/v7/widget/Toolbar.java#963
 */
internal val appCompatToolbarBackButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/graphics/drawable/Drawable;")
    parameters()
    custom { _, classDef ->
        classDef.type == "Landroid/support/v7/widget/Toolbar;"
    }
}

/**
 * Matches to the class found in [pivotBarConstructorFingerprint].
 */
internal val initializeButtonsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        resourceLiteral(ResourceType.LAYOUT, "image_only_tab")
    )
}

/**
 * Extension method, used for callback into to other patches.
 * Specifically, [navigationButtonsPatch].
 */
internal val navigationBarHookCallbackFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("V")
    parameters(EXTENSION_NAVIGATION_BUTTON_DESCRIPTOR, "Landroid/view/View;")
    custom { method, _ ->
        method.name == "navigationTabCreatedCallback" &&
            method.definingClass == EXTENSION_CLASS_DESCRIPTOR
    }
}

/**
 * Matches to the Enum class that looks up ordinal -> instance.
 */
internal val navigationEnumFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings(
        "PIVOT_HOME",
        "TAB_SHORTS",
        "CREATION_TAB_LARGE",
        "PIVOT_SUBSCRIPTIONS",
        "TAB_ACTIVITY",
        "VIDEO_LIBRARY_WHITE",
        "INCOGNITO_CIRCLE",
    )
    custom { _, classDef ->
        // Don't match our own code.
        !classDef.type.startsWith("Lapp/revanced")
    }
}

internal val pivotBarButtonsCreateDrawableViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;" &&
            // Only one view creation method has a Drawable parameter.
            method.parameterTypes.firstOrNull() == "Landroid/graphics/drawable/Drawable;"
    }
}

internal val pivotBarButtonsCreateResourceStyledViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "Z", "I", "L")
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;"
    }
}

/**
 * 20.21+
 */
internal val pivotBarButtonsCreateResourceIntViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;" &&
            // Only one view creation method has an int first parameter.
            method.parameterTypes.firstOrNull() == "I"
    }
}

internal val pivotBarButtonsViewSetSelectedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("I", "Z")
    instructions(
        methodCall(name = "setSelected")
    )
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;"
    }
}

internal val pivotBarConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        string("com.google.android.apps.youtube.app.endpoint.flags"),
    )
}

internal val imageEnumConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    instructions(
        string("TAB_ACTIVITY_CAIRO"),
        opcode(Opcode.SPUT_OBJECT)
    )
    custom { _, classDef ->
        // Don't match our extension code.
        !classDef.type.startsWith("Lapp/revanced/")
    }
}

internal val setEnumMapFingerprint = fingerprint {
    instructions(
        resourceLiteral(ResourceType.DRAWABLE, "yt_fill_bell_black_24"),
        methodCall(smali = "Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;", location = MatchAfterWithin(10)),
        methodCall(smali = "Ljava/util/EnumMap;->put(Ljava/lang/Enum;Ljava/lang/Object;)Ljava/lang/Object;", location = MatchAfterWithin(10))
    )
}
