package app.revanced.patches.gmxmail.freephone

import app.revanced.patcher.fingerprint

internal val isEuiccEnabledFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "isEuiccEnabled"
    }
}