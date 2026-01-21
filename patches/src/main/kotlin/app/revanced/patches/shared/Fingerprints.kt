package app.revanced.patches.shared

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.castContextFetchMethod by gettingFirstMutableMethodDeclaratively(
    "Error fetching CastContext."
)

internal val BytecodePatchContext.primeMethod by gettingFirstMutableMethodDeclaratively(
    "com.android.vending",
    "com.google.android.GoogleCamera"
)
