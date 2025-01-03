package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val reelEnumConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    opcodes(Opcode.RETURN_VOID)
    strings(
        "REEL_LOOP_BEHAVIOR_UNKNOWN",
        "REEL_LOOP_BEHAVIOR_SINGLE_PLAY",
        "REEL_LOOP_BEHAVIOR_REPEAT",
        "REEL_LOOP_BEHAVIOR_END_SCREEN",
    )
}

internal val reelPlaybackRepeatFingerprint by fingerprint {
    returns("V")
    parameters("L")
    strings("YoutubePlayerState is in throwing an Error.")
}
