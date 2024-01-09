package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object AdPersonalizationActivityOnCreateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/AdPersonalizationActivity;") &&
                methodDef.name == "onCreate"
    }
)