package app.revanced.patches.hexeditor.ad.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val primaryAdsFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("PreferencesHelper;") && methodDef.name == "isAdsDisabled"
    }
}