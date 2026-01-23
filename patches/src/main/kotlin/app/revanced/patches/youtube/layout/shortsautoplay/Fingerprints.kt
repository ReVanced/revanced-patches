package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.addString
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val reelEnumConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    instructions(
        addString("REEL_LOOP_BEHAVIOR_UNKNOWN"),
        addString("REEL_LOOP_BEHAVIOR_SINGLE_PLAY"),
        addString("REEL_LOOP_BEHAVIOR_REPEAT"),
        addString("REEL_LOOP_BEHAVIOR_END_SCREEN"),
        opcode(Opcode.RETURN_VOID),
    )
}

internal val reelPlaybackRepeatParentFingerprint = fingerprint {
    returnType("V")
    parameterTypes("Ljava/lang/String;", "J")
    instructions(
        addString("Reels[%s] Playback Time: %d ms"),
    )
}

/**
 * Matches class found in [reelPlaybackRepeatParentFingerprint].
 */
internal val reelPlaybackRepeatFingerprint = fingerprint {
    returnType("V")
    parameterTypes("L")
    instructions(
        methodCall(smali = "Lcom/google/common/util/concurrent/ListenableFuture;->isDone()Z"),
    )
}

internal val reelPlaybackFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("J")
    returnType("V")
    instructions(
        fieldAccess(
            definingClass = "Ljava/util/concurrent/TimeUnit;",
            name = "MILLISECONDS",
        ),
        methodCall(
            name = "<init>",
            parameters = listOf("I", "L", "L"),
            location = MatchAfterWithin(15),
        ),
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            parameters = listOf("L"),
            returnType = "I",
            location = MatchAfterWithin(5),
        ),
    )
}
