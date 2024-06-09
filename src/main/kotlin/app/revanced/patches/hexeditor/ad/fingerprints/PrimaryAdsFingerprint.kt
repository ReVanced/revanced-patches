package app.revanced.patches.hexeditor.ad.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val primaryAdsFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("PreferencesHelper;") && methodDef.name == "isAdsDisabled"
    }
}