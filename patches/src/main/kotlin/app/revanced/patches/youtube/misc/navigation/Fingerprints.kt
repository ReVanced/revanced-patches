package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.*
import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val actionBarSearchResultsMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    instructions(
        ResourceType.LAYOUT("action_bar_search_results_view_mic"),
        method("setLayoutDirection"),
    )
}

internal val toolbarLayoutMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.ID("toolbar_container"),
        allOf(
            Opcode.CHECK_CAST(),
            type("Lcom/google/android/apps/youtube/app/ui/actionbar/MainCollapsingToolbarLayout;")
        )
    )
}

/**
 * Matches to https://android.googlesource.com/platform/frameworks/support/+/9eee6ba/v7/appcompat/src/android/support/v7/widget/Toolbar.java#963
 */
internal val BytecodePatchContext.appCompatToolbarBackButtonMethod by gettingFirstMethodDeclaratively {
    definingClass("Landroid/support/v7/widget/Toolbar;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/graphics/drawable/Drawable;")
    parameterTypes()
}

/**
 * Matches to the class found in [pivotBarConstructorMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getInitializeButtonsMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        ResourceType.LAYOUT("image_only_tab"),
    )
}

/**
 * Extension method, used for callback into to other patches.
 * Specifically, [navigationButtonsPatch].
 */
internal val BytecodePatchContext.navigationBarHookCallbackMethod by gettingFirstMutableMethodDeclaratively {
    name("navigationTabCreatedCallback")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("V")
    parameterTypes(EXTENSION_NAVIGATION_BUTTON_DESCRIPTOR, "Landroid/view/View;")
}

/**
 * Matches to the Enum class that looks up ordinal -> instance.
 */
internal val BytecodePatchContext.navigationEnumMethod by gettingFirstMethodDeclaratively(
    "PIVOT_HOME",
    "TAB_SHORTS",
    "CREATION_TAB_LARGE",
    "PIVOT_SUBSCRIPTIONS",
    "TAB_ACTIVITY",
    "VIDEO_LIBRARY_WHITE",
    "INCOGNITO_CIRCLE",
    "UNKNOWN", // Required to distinguish from patch extension class.
) {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.pivotBarButtonsCreateDrawableViewMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    custom {
        // Only one view creation method has a Drawable parameter.
        parameterTypes.firstOrNull() == "Landroid/graphics/drawable/Drawable;"
    }
}

internal val BytecodePatchContext.pivotBarButtonsCreateResourceStyledViewMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes("L", "Z", "I", "L")
}

/**
 * 20.21+
 */
internal val BytecodePatchContext.pivotBarButtonsCreateResourceIntViewMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    custom {
        // Only one view creation method has an int first parameter.
        parameterTypes.firstOrNull() == "I"
    }
}

internal val pivotBarButtonsViewSetSelectedMethodMatch = firstMethodComposite {
    definingClass("Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("I", "Z")
    instructions(method("setSelected"))
}

internal val BytecodePatchContext.pivotBarConstructorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        "com.google.android.apps.youtube.app.endpoint.flags"(),
    )
}

internal val imageEnumConstructorMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    instructions(
        "TAB_ACTIVITY_CAIRO"(),
        after(Opcode.INVOKE_DIRECT()),
        after(Opcode.SPUT_OBJECT()),
    )
}

internal val setEnumMapMethodMatch = firstMethodComposite {
    instructions(
        ResourceType.DRAWABLE("yt_fill_bell_black_24"),
        afterAtMost(10, method { toString() == "Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;" }),
        afterAtMost(
            10,
            method { toString() == "Ljava/util/EnumMap;->put(Ljava/lang/Enum;Ljava/lang/Object;)Ljava/lang/Object;" }
        )
    )
}
