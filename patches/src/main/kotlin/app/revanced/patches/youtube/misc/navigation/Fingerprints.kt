package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.fingerprint
import app.revanced.patches.youtube.layout.buttons.navigation.navigationButtonsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val actionBarSearchResultsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    literal { actionBarSearchResultsViewMicId }
}

internal val toolbarLayoutFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.CONSTRUCTOR)
    literal { toolbarContainerId }
}

/**
 * Matches to https://android.googlesource.com/platform/frameworks/support/+/9eee6ba/v7/appcompat/src/android/support/v7/widget/Toolbar.java#963
 */
internal val appCompatToolbarBackButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/graphics/drawable/Drawable;")
    parameters()
    custom { methodDef, classDef ->
        classDef.type == "Landroid/support/v7/widget/Toolbar;"
    }
}

/**
 * Matches to the class found in [pivotBarConstructorFingerprint].
 */
internal val initializeButtonsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    literal { imageOnlyTabResourceId }
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
}

internal val pivotBarButtonsCreateDrawableViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;" &&
            // Only one method has a Drawable parameter.
            method.parameterTypes.firstOrNull() == "Landroid/graphics/drawable/Drawable;"
    }
}

internal val pivotBarButtonsCreateResourceViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "Z", "I", "L")
    custom { method, _ ->
        method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;"
    }
}

internal fun indexOfSetViewSelectedInstruction(method: Method) = method.indexOfFirstInstruction {
    opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.name == "setSelected"
}

internal val pivotBarButtonsViewSetSelectedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("I", "Z")
    custom { method, _ ->
        indexOfSetViewSelectedInstruction(method) >= 0 &&
            method.definingClass == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;"
    }
}

internal val pivotBarConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    strings("com.google.android.apps.youtube.app.endpoint.flags")
}

internal val imageEnumConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings("TAB_ACTIVITY_CAIRO")
}

internal val setEnumMapFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    literal {
        ytFillBellId
    }
}
