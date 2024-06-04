package app.revanced.patches.youtube.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val licenseActivityOnCreateFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    custom { methodDef, classDef ->
        classDef.endsWith("LicenseActivity;") && methodDef.name == "onCreate"
    }
}
