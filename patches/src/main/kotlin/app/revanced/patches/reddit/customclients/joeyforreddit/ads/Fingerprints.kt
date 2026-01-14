package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isAdFreeUserMethod by gettingFirstMutableMethodDeclaratively("AD_FREE_USER") {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Z")
}
