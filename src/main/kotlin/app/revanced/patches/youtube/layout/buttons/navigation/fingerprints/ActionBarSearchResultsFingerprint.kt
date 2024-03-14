package app.revanced.patches.youtube.layout.buttons.navigation.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.buttons.navigation.ResolvePivotBarFingerprintsPatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object ActionBarSearchResultsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Landroid/view/View;",
    parameters = listOf("Landroid/view/LayoutInflater;"),
    literalSupplier = { ResolvePivotBarFingerprintsPatch.actionBarSearchResultsViewMicId }
)