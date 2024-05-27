package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patches.youtube.misc.navigation.actionBarSearchResultsViewMicId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val actionBarSearchResultsFingerprint = literalValueFingerprint(
    literalSupplier = { actionBarSearchResultsViewMicId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/view/LayoutInflater;")
}
