package app.revanced.patches.hexeditor.ad

import app.revanced.patcher.fingerprint

internal val primaryAdsFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("PreferencesHelper;") && method.name == "isAdsDisabled"
    }
}
