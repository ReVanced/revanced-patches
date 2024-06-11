package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.miniplayer.floatyBarButtonTopMargin
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val miniplayerDimensionsCalculatorParentFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    literal { floatyBarButtonTopMargin }
}
