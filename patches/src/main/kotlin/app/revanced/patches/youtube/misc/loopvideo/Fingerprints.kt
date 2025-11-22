package app.revanced.patches.youtube.misc.loopvideo

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal val videoStartPlaybackFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        string("play() called when the player wasn't loaded."),
        string("play() blocked because Background Playability failed")
    )
}
