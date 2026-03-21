package app.revanced.patches.youtube.layout.shortsresuming

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import kotlin.collections.all
import kotlin.collections.zip


/**
 * 21.03+
 */
internal val BytecodePatchContext.userWasInShortsEvaluateMethodMatch by composingFirstMethod {
    val method1ParametersPrefix = listOf("L", "Z", "Z", "L", "Z")
    val method2ParametersPrefix = listOf("L", "L", "L", "L", "L", "I")

    instructions(
        allOf(
            Opcode.INVOKE_DIRECT_RANGE(),
            method {
                name == "<init>" && parameterTypes.zip(method1ParametersPrefix)
                    .all { (a, b) -> a.startsWith(b) }
            }
        ),
        afterAtMost(
            50, allOf(
                Opcode.INVOKE_DIRECT_RANGE(),
                method {
                    name == "<init>" && parameterTypes.zip(method2ParametersPrefix)
                        .all { (a, b) -> a.startsWith(b) }
                }
            )
        )
    )
}

/**
 * 20.02+
 */
internal
val BytecodePatchContext.userWasInShortsListenerMethodMatch by composingFirstMethod {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Ljava/lang/Object;")
    instructions(
        allOf(Opcode.CHECK_CAST(), type("Ljava/lang/Boolean;")),
        after(method { toString() == "Ljava/lang/Boolean;->booleanValue()Z" }),
        after(Opcode.MOVE_RESULT()),
        // 20.40+ string was merged into another string and is a partial match.
        afterAtMost(30, "ShortsStartup SetUserWasInShortsListener"(String::contains)),
    )
}

/**
 * Pre 20.02
 */
internal
val BytecodePatchContext.userWasInShortsLegacyMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Ljava/lang/Object;")
    instructions(
        "Failed to read user_was_in_shorts proto after successful warmup"(),
    )
}

/**
 * 18.15.40+
 */
internal
val BytecodePatchContext.userWasInShortsConfigMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45358360L(),
    )
}
