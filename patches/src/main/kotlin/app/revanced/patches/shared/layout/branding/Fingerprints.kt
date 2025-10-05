package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.fingerprint

internal val customIconIncludedFingerprint = fingerprint {
    returns("Z")
    parameters()
    custom { method, classDef ->
        method.name == "customIconIncluded" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}