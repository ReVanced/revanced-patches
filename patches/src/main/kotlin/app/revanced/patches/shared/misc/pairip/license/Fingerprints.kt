package app.revanced.patches.shared.misc.pairip.license

import app.revanced.patcher.fingerprint

internal val processLicenseResponseFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/pairip/licensecheck/LicenseClient;" &&
                method.name == "processResponse"
    }
}

internal val validateLicenseResponseFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/pairip/licensecheck/ResponseValidator;" &&
                method.name == "validateResponse"
    }
}
