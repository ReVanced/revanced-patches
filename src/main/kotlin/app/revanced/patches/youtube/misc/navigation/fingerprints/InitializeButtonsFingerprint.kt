package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patches.youtube.misc.navigation.imageOnlyTabResourceId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves to the class found in [pivotBarConstructorFingerprint].
 */
internal val initializeButtonsFingerprint = literalValueFingerprint(
    literalSupplier = { imageOnlyTabResourceId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
}
