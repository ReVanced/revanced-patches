package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val pivotBarButtonsCreateDrawableViewFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    custom { methodDef, classDef ->
        classDef.type == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;" &&
            // Only one method has a Drawable parameter.
            methodDef.parameterTypes.firstOrNull() == "Landroid/graphics/drawable/Drawable;"
    }
}
