package app.revanced.patches.youtube.misc.settings.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object LicenseActivityFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("L"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("LicenseActivity;") && methodDef.name == "onCreate"
    }
)