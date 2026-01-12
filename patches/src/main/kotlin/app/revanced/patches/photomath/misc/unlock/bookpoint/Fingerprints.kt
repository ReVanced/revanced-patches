package app.revanced.patches.photomath.misc.unlock.bookpoint

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isBookpointEnabledMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        "NoGeoData"(),
        "NoCountryInGeo"(),
        "RemoteConfig"(),
        "GeoRCMismatch"()
    )
}
