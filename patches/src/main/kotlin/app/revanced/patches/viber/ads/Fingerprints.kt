package app.revanced.patches.viber.ads

import app.revanced.patcher.fingerprint

internal val adsFreeFingerprint = fingerprint {
    returns("I")
    parameters()
    custom { method, classDef ->
        classDef.type.contains("com/viber/voip/feature/viberplus") &&
        classDef.superclass?.contains("com/viber/voip/core/feature") == true &&  // Must extend com.viber.voip.core.feature.?
        classDef.methods.count() == 1
    }
}
