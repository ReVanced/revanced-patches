package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.misc.navigation.imageOnlyTabResourceId
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves to the class found in [pivotBarConstructorFingerprint].
 */
internal val initializeButtonsFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    literal { imageOnlyTabResourceId }
}
