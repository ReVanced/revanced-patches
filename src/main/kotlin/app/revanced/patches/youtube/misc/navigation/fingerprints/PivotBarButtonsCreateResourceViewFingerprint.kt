package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PivotBarButtonsCreateResourceViewFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "Z", "I", "L"),
    returnType = "Landroid/view/View;",
    customFingerprint = { _, classDef ->
        classDef.type == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;"
    }
)