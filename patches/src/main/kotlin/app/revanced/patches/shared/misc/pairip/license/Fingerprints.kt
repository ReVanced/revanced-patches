package app.revanced.patches.shared.misc.pairip.license

import app.revanced.patcher.fingerprint

internal val processLicenseResponseFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/pairip/licensecheck/LicenseClient;" &&
                method.name == "processResponse"
    }
}

internal val validateLicenseResponseFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/pairip/licensecheck/ResponseValidator;" &&
                method.name == "validateResponse"
    }
}
