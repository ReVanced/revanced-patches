package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val adPersonalizationActivityOnCreateFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/AdPersonalizationActivity;") &&
            methodDef.name == "onCreate"
    }
}
