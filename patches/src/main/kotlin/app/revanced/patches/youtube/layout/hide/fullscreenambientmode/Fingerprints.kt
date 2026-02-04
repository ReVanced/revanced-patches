package app.revanced.patches.youtube.layout.hide.fullscreenambientmode

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.setFullScreenBackgroundColorMethod by gettingFirstMethodDeclaratively {
    name("onLayout")
    definingClass { endsWith("/YouTubePlayerViewNotForReflection;") }
    returnType("V")
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    parameterTypes("Z", "I", "I", "I", "I")
}
