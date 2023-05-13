package app.revanced.patches.youtube.misc.settings.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object LicenseActivityFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("LicenseActivity;") && methodDef.name == "onCreate"
    }
)