package app.revanced.patches.youtube.misc.fix.cairo.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.misc.fix.cairo.fingerprints.CarioFragmentConfigFingerprint.CAIRO_CONFIG_LITERAL_VALUE
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Added in YouTube v19.04.38
 *
 * When this value is TRUE, Cairo Fragment is used.
 * In this case, some of patches may be broken, so set this value to FALSE.
 */
internal object CarioFragmentConfigFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    literalSupplier = { CAIRO_CONFIG_LITERAL_VALUE }
) {
  const val CAIRO_CONFIG_LITERAL_VALUE = 45532100L
}
