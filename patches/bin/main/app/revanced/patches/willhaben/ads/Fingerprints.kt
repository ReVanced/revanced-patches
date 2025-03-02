package app.revanced.patches.willhaben.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val adResolverFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters("L", "L")
    strings(
        "Google Ad is invalid ",
        "Google Native Ad is invalid ",
        "Criteo Ad is invalid ",
        "Amazon Ad is invalid ",
    )
}

internal val whAdViewInjectorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L", "L", "Z")
    strings("successfulAdView")
    custom { _, classDef ->
        classDef.type == "Lat/willhaben/advertising/WHAdView;"
    }
}
