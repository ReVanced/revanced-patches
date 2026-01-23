package app.revanced.patches.youtube.misc.loopvideo

import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.videoStartPlaybackMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        addString("play() called when the player wasn't loaded."),
        addString("play() blocked because Background Playability failed"),
    )
}
