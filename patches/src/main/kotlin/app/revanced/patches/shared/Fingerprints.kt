package app.revanced.patches.shared

import app.revanced.patcher.accessFlags
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.castContextFetchMethod by gettingFirstMethodDeclaratively(
    "Error fetching CastContext."
)

internal val BytecodePatchContext.primeMethod by gettingFirstMethodDeclaratively(
    "com.android.vending",
    "com.google.android.GoogleCamera"
)

// Flag is present in YouTube 20.23, but bold icons are missing and forcing them crashes the app.
// 20.31 is the first target with all the bold icons present.
internal val BytecodePatchContext.boldIconsFeatureFlagMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45685201L(),
    )
}
