package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object AdPersonalizationActivityOnCreateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/AdPersonalizationActivity;") &&
                methodDef.name == "onCreate"
    }
)