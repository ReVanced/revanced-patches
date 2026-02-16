package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.thumbnail

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.customImageViewLoadMethod by gettingFirstMethodDeclaratively {
    definingClass("CustomImageView;")
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes("Ljava/lang/String;", "Z", "Z", "I", "I")
}
