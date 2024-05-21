package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val pivotBarButtonsCreateResourceViewFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "Z", "I", "L")
    custom { _, classDef ->
        classDef.type == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;"
    }
}
