package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val licenseActivityOnCreateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    custom { method, classDef ->
        classDef.endsWith("LicenseActivity;") && method.name == "onCreate"
    }
}

internal val setThemeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    literal { appearanceStringId }
}

internal const val CAIRO_CONFIG_LITERAL_VALUE = 45532100L

internal val cairoFragmentConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal { CAIRO_CONFIG_LITERAL_VALUE }
}
