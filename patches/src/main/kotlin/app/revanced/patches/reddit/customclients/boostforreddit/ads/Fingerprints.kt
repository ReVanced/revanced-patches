package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.maxMediationMethod by gettingFirstMethodDeclaratively(
    "MaxMediation: Attempting to initialize SDK"
)

internal val BytecodePatchContext.admobMediationMethod by gettingFirstMethodDeclaratively(
    "AdmobMediation: Attempting to initialize SDK"
)
