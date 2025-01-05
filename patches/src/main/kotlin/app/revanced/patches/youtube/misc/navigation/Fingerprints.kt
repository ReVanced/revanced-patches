package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.MethodCallFilter
import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceMappingFilter
import app.revanced.patches.youtube.layout.buttons.navigation.navigationButtonsPatch
import com.android.tools.smali.dexlib2.AccessFlags

internal val actionBarSearchResultsFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    instructions(
        ResourceMappingFilter("layout", "action_bar_search_results_view_mic"),
        MethodCallFilter(methodName = "setLayoutDirection")
    )
}

/**
 * Matches to the class found in [pivotBarConstructorFingerprint].
 */
internal val initializeButtonsFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        ResourceMappingFilter("layout", "image_only_tab")
    )
}

internal val mainActivityOnBackPressedFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { method, classDef ->
        val matchesClass = classDef.endsWith("MainActivity;") ||
            // Old versions of YouTube called this class "WatchWhileActivity" instead.
            classDef.endsWith("WatchWhileActivity;")

        matchesClass && method.name == "onBackPressed"
    }
}

/**
 * Extension method, used for callback into to other patches.
 * Specifically, [navigationButtonsPatch].
 */
internal val navigationBarHookCallbackFingerprint by fingerprint {
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
internal val navigationEnumFingerprint by fingerprint {
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
}

internal val pivotBarButtonsCreateDrawableViewFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;" &&
            // Only one method has a Drawable parameter.
            method.parameterTypes.firstOrNull() == "Landroid/graphics/drawable/Drawable;"
    }
}

internal val pivotBarButtonsCreateResourceViewFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "Z", "I", "L")
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;"
    }
}

internal val pivotBarButtonsViewSetSelectedFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("I", "Z")
    instructions(
        MethodCallFilter(methodName = "setSelected")
    )
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;"
    }
}

internal val pivotBarConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    strings("com.google.android.apps.youtube.app.endpoint.flags")
}

internal val imageEnumConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings("TAB_ACTIVITY_CAIRO")
}

internal val setEnumMapFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceMappingFilter("drawable", "yt_fill_bell_black_24")
    )
}
