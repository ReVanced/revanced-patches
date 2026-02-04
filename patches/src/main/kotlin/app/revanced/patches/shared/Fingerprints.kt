package app.revanced.patches.shared

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.castContextFetchMethod by gettingFirstMethodDeclaratively(
    "Error fetching CastContext."
)

internal val BytecodePatchContext.primeMethod by gettingFirstMethodDeclaratively(
    "com.android.vending",
    "com.google.android.GoogleCamera"
)
