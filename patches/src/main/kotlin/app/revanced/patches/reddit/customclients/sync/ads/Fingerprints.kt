package app.revanced.patches.reddit.customclients.sync.ads

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.isAdsEnabledMethod by gettingFirstMutableMethodDeclaratively("SyncIapHelper") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
}
