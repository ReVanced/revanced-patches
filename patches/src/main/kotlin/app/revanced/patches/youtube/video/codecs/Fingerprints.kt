package app.revanced.patches.youtube.video.codecs

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.vp9CapabilityMethod by gettingFirstMethodDeclaratively(
    "vp9_supported",
    "video/x-vnd.on2.vp9",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
}
