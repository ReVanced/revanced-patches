package app.revanced.patches.fotmob.ads

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val shouldDisplayAdsMethod = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    custom { method, classDef ->
        method.name == "shouldDisplayAds" && classDef.type.endsWith("AdsService;")
    }
}