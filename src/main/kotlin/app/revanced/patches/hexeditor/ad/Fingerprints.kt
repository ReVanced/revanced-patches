package app.revanced.patches.hexeditor.ad

import app.revanced.patcher.fingerprint.methodFingerprint

internal val primaryAdsFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("PreferencesHelper;") && methodDef.name == "isAdsDisabled"
    }
}