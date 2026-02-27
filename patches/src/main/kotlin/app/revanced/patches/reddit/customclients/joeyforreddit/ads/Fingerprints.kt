package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isAdFreeUserMethod by gettingFirstMethodDeclaratively("AD_FREE_USER") {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Z")
}
