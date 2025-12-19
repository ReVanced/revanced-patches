package app.revanced.patches.com.sbs.ondemand.tv

import app.revanced.patcher.fingerprint

// Advertisement-related fingerprints
internal val shouldShowAdvertisingTVFingerprint = fingerprint {
    returns("Z")
    custom { method, classDef ->
        method.name == "getShouldShowAdvertisingTV" &&
        classDef.type == "Lcom/sbs/ondemand/common/PreferencesStorage;"
    }
}

internal val shouldShowPauseAdFingerprint = fingerprint {
    returns("Z")
    custom { method, classDef ->
        method.name == "shouldShowPauseAd" &&
        classDef.type == "Lcom/sbs/ondemand/player/viewmodels/PauseAdController;"
    }
}

internal val requestAdStreamFingerprint = fingerprint {
    returns("V")
    custom { method, classDef ->
        method.name == "requestAdStream" &&
        classDef.type == "Lcom/sbs/ondemand/player/viewmodels/AdsController;"
    }
}

// License-related fingerprints
internal val licenseContentProviderOnCreateFingerprint = fingerprint {
    returns("Z")
    custom { method, classDef ->
        method.name == "onCreate" &&
        classDef.type == "Lcom/pairip/licensecheck/LicenseContentProvider;"
    }
}

internal val initializeLicenseCheckFingerprint = fingerprint {
    returns("V")
    custom { method, classDef ->
        method.name == "initializeLicenseCheck" &&
        classDef.type == "Lcom/pairip/licensecheck/LicenseClient;"
    }
}

// Analytics-related fingerprints
internal val convivaConfigGetHBIntervalFingerprint = fingerprint {
    returns("I")
    parameters("I")
    custom { method, classDef ->
        method.name == "getHBInterval" &&
        classDef.type == "Lcom/conviva/utils/Config;"
    }
}
