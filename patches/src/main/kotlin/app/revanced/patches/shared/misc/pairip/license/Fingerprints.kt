package app.revanced.patches.shared.misc.pairip.license

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclarativelyOrNull
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.processLicenseResponseMethod by gettingFirstMethodDeclarativelyOrNull {
    name("processResponse")
    definingClass("Lcom/pairip/licensecheck/LicenseClient;")
}

internal val BytecodePatchContext.validateLicenseResponseMethod by gettingFirstMethodDeclarativelyOrNull {
    name("validateResponse")
    definingClass("Lcom/pairip/licensecheck/ResponseValidator;")
}
