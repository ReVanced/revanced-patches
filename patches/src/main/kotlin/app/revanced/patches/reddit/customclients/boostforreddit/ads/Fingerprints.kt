package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.maxMediationMethod by gettingFirstMutableMethodDeclaratively(
    "MaxMediation: Attempting to initialize SDK"
)

internal val BytecodePatchContext.admobMediationMethod by gettingFirstMutableMethodDeclaratively(
    "AdmobMediation: Attempting to initialize SDK"
)
