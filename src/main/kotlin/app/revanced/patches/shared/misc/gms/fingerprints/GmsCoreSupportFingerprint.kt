package app.revanced.patches.shared.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

const val GET_GMS_CORE_VENDOR_GROUP_ID_METHOD_NAME = "getGmsCoreVendorGroupId"

internal val gmsCoreSupportFingerprint = methodFingerprint {
    custom { _, classDef ->
        classDef.endsWith("GmsCoreSupport;")
    }
}
