package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.string
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
    instructions(
        string("YoutubePlayerState is in throwing an Error.")
    )
}

internal val reelPlaybackFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("J")
    returns("V")
    instructions(
        fieldAccess(definingClass = "Ljava/util/concurrent/TimeUnit;", name = "MILLISECONDS"),
        methodCall(name = "<init>", parameters = listOf("I", "L", "L"), maxAfter = 15),
        methodCall(opcode = Opcode.INVOKE_VIRTUAL, parameters = listOf("L"), returnType = "I", maxAfter = 5)
    )
}