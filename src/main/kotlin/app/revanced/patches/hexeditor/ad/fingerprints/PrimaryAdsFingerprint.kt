package app.revanced.patches.hexeditor.ad.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object PrimaryAdsFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        classDef.endsWith("PreferencesHelper;") && methodDef.name == "isAdsDisabled"
    }
)