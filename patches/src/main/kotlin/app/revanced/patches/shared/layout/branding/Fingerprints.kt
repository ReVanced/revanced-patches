package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.fingerprint

internal val customNumberOfNamesIncludingDummyAliasesFingerprint = fingerprint {
    returns("I")
    parameters()
    custom { method, classDef ->
        method.name == "numberOfCustomNamesIncludingDummyAliases" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val customNumberOfNamesFingerprint = fingerprint {
    returns("I")
    parameters()
    custom { method, classDef ->
        method.name == "numberOfCustomNames" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
