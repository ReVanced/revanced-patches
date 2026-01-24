package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.reelEnumConstructorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    instructions(
        "REEL_LOOP_BEHAVIOR_UNKNOWN"(),
        "REEL_LOOP_BEHAVIOR_SINGLE_PLAY"(),
        "REEL_LOOP_BEHAVIOR_REPEAT"(),
        "REEL_LOOP_BEHAVIOR_END_SCREEN"(),
        Opcode.RETURN_VOID(),
    )
}

internal val BytecodePatchContext.reelPlaybackRepeatParentMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes("Ljava/lang/String;", "J")
    instructions(
        "Reels[%s] Playback Time: %d ms"(),
    )
}

/**
 * Matches class found in [reelPlaybackRepeatParentMethod].
 */
internal val BytecodePatchContext.reelPlaybackRepeatMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes("L")
    instructions(
        methodCall(smali = "Lcom/google/common/util/concurrent/ListenableFuture;->isDone()Z"),
    )
}

internal val BytecodePatchContext.reelPlaybackMethod by gettingFirstMethodDeclaratively {
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
            afterAtMost(15),
        ),
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            parameters = listOf("L"),
            returnType = "I",
            afterAtMost(5),
        ),
    )
}
