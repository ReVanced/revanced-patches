package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.reelEnumConstructorMethodMatch by composingFirstMethod {
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
context(_: BytecodePatchContext)
internal fun ClassDef.getReelPlaybackRepeatMethod() = firstMutableMethodDeclaratively {
    returnType("V")
    parameterTypes("L")
    instructions(method { toString() == "Lcom/google/common/util/concurrent/ListenableFuture;->isDone()Z" })
}

internal val BytecodePatchContext.reelPlaybackMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("J")
    returnType("V")

    val methodParametersPrefix = listOf("I", "L", "L")
    instructions(
        field { definingClass == "Ljava/util/concurrent/TimeUnit;" && name == "MILLISECONDS" },
        afterAtMost(
            15,
            method {
                name == "<init>" &&
                    parameterTypes.zip(methodParametersPrefix).all { (a, b) -> a.startsWith(b) }
            },
        ),
        afterAtMost(
            5,
            allOf(
                Opcode.INVOKE_VIRTUAL(),
                method { returnType == "I" && parameterTypes.count() == 1 && parameterTypes.first() == "L" },
            ),
        ),
    )
}
