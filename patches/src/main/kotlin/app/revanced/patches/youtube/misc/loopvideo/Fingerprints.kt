package app.revanced.patches.youtube.misc.loopvideo

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.videoStartPlaybackMethod by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        "play() called when the player wasn't loaded."(),
        "play() blocked because Background Playability failed"(),
    )
}
