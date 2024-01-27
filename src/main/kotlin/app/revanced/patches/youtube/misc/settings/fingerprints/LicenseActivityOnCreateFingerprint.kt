package app.revanced.patches.youtube.misc.settings.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object LicenseActivityOnCreateFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("L"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("LicenseActivity;") && methodDef.name == "onCreate"
    }
)