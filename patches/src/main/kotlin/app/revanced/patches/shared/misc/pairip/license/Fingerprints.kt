package app.revanced.patches.shared.misc.pairip.license

internal val BytecodePatchContext.processLicenseResponseMethod by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        classDef.type == "Lcom/pairip/licensecheck/LicenseClient;" &&
            method.name == "processResponse"
    }
}

internal val BytecodePatchContext.validateLicenseResponseMethod by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        classDef.type == "Lcom/pairip/licensecheck/ResponseValidator;" &&
            method.name == "validateResponse"
    }
}
