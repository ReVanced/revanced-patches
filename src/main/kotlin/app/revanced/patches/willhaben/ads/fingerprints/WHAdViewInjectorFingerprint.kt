package app.revanced.patches.willhaben.ads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object WHAdViewInjectorFingerprint : MethodFingerprint(
    "V",
    parameters = listOf("L", "L", "L", "Z"),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("successfulAdView"),
    customFingerprint = { _, classDef ->
        classDef.type == "Lat/willhaben/advertising/WHAdView;"
    }
)
