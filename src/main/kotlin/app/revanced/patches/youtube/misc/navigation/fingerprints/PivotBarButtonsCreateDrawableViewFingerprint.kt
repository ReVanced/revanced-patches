package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PivotBarButtonsCreateDrawableViewFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    // Method has different number of parameters in some app targets.
    // Parameters are checked in custom fingerprint.
    returnType = "Landroid/view/View;",
    customFingerprint = { methodDef, classDef ->
        classDef.type == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;" &&
                // Only one method has a Drawable parameter.
                methodDef.parameterTypes.firstOrNull() == "Landroid/graphics/drawable/Drawable;"
    }
)
