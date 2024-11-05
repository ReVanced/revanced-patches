package app.revanced.patches.youtube.misc.fix.cairo

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Added in YouTube v19.04.38.
 *
 * When this value is true, Cairo Fragment is used.
 * In this case, some of the patches may be broken, so set this value to FALSE.
 */
internal const val CAIRO_CONFIG_LITERAL_VALUE = 45532100L

internal val cairoFragmentConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal { CAIRO_CONFIG_LITERAL_VALUE }
}
