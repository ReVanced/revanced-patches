package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.miniplayer.floatyBarButtonTopMargin
import com.android.tools.smali.dexlib2.AccessFlags

internal val miniplayerDimensionsCalculatorParentFingerprint = methodFingerprint(
    literal { floatyBarButtonTopMargin },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
}
