package app.revanced.patches.youtube.layout.hide.fullscreenambientmode

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.setFullScreenBackgroundColorMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    parameterTypes("Z", "I", "I", "I", "I")
    custom { method, classDef ->
        classDef.type.endsWith("/YouTubePlayerViewNotForReflection;") &&
            method.name == "onLayout"
    }
}
