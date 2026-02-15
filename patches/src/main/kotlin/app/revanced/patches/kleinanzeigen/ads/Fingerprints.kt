package app.revanced.patches.kleinanzeigen.ads

import app.revanced.patcher.fingerprint

internal val getLibertyInitFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "init" && classDef.endsWith("/Liberty;")
    }
}
