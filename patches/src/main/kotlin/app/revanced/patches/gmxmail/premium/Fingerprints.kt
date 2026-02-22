package app.revanced.patches.gmxmail.premium

import app.revanced.patcher.fingerprint

internal val isUpsellingPossibleFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "isUpsellingPossible" && classDef.endsWith("/PayMailManager;")
    }
}