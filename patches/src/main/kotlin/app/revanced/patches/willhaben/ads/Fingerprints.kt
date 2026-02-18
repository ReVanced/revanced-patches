package app.revanced.patches.willhaben.ads

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.adResolverMethod by gettingFirstMethodDeclaratively(
    "Google Ad is invalid ",
    "Google Native Ad is invalid ",
    "Criteo Ad is invalid ",
    "Amazon Ad is invalid ",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("L", "L")

}

internal val BytecodePatchContext.whAdViewInjectorMethod by gettingFirstMethodDeclaratively("successfulAdView") {
    definingClass("Lat/willhaben/advertising/WHAdView;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "L", "L", "Z")
}
