package app.revanced.patches.pixiv.ads

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val shouldShowAdsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    custom { methodDef, classDef ->
        classDef.type.endsWith("AdUtils;") && methodDef.name == "shouldShowAds"
    }
}
