package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val licenseActivityOnCreateFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    custom { methodDef, classDef ->
        classDef.endsWith("LicenseActivity;") && methodDef.name == "onCreate"
    }
}

internal val setThemeFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    literal { appearanceStringId }
}
