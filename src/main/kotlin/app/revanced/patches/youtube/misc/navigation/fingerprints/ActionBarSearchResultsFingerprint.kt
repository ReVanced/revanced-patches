package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.misc.navigation.actionBarSearchResultsViewMicId
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val actionBarSearchResultsFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/view/LayoutInflater;")
    literal { actionBarSearchResultsViewMicId }
}
