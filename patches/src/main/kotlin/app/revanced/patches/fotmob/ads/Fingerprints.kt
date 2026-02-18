package app.revanced.patches.fotmob.ads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.shouldDisplayAdsMethod by gettingFirstMethodDeclaratively {
    name("shouldDisplayAds")
    definingClass("AdsService;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
}