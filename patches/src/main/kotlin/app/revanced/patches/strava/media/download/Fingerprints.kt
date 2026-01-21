package app.revanced.patches.strava.media.download

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val createAndShowFragmentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    strings("mediaType")
}

internal val handleMediaActionFingerprint = fingerprint {
    parameters("Landroid/view/View;", "Lcom/strava/bottomsheet/BottomSheetItem;")
}
