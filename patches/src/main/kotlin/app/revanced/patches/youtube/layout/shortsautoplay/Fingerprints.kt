package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val reelEnumConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    instructions(
        string("REEL_LOOP_BEHAVIOR_UNKNOWN"),
        string("REEL_LOOP_BEHAVIOR_SINGLE_PLAY"),
        string("REEL_LOOP_BEHAVIOR_REPEAT"),
        string("REEL_LOOP_BEHAVIOR_END_SCREEN"),
        opcode(Opcode.RETURN_VOID)
    )
}

internal val reelPlaybackRepeatParentFingerprint = fingerprint {
    returns("V")
    parameters("Ljava/lang/String;", "J")
    instructions(
        string("Reels[%s] Playback Time: %d ms")
    )
}

/**
 * Matches class found in [reelPlaybackRepeatParentFingerprint].
 */
internal val reelPlaybackRepeatFingerprint = fingerprint {
    returns("V")
    parameters("L")
    instructions(
        methodCall(smali = "Lcom/google/common/util/concurrent/ListenableFuture;->isDone()Z")
    )
}

internal val reelPlaybackFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("J")
    returns("V")
    instructions(
        fieldAccess(
            definingClass = "Ljava/util/concurrent/TimeUnit;",
            name = "MILLISECONDS"
        ),
        methodCall(
            name = "<init>",
            parameters = listOf("I", "L", "L"),
            location = MatchAfterWithin(15)
        ),
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            parameters = listOf("L"),
            returnType = "I",
            location = MatchAfterWithin(5)
        )
    )
}