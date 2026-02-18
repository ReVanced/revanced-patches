package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.useGradientLoadingScreenMethodMatch by composingFirstMethod {
    instructions(45412406L())
}

internal val BytecodePatchContext.splashScreenStyleMethodMatch by composingFirstMethod {
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    name("onCreate")
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
    instructions(
        anyOf(
            1074339245L(), // 20.30+
            269032877L(), // 20.29 and lower.
        ),
    )
}


/**
 * Matches to the same method as [splashScreenStyleMethodMatch].
 */
internal val BytecodePatchContext.showSplashScreen1MethodMatch by composingFirstMethod {
    name("onCreate")
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
    instructions(
        anyOf(Opcode.CONST_4(), Opcode.CONST_16()),
        afterAtMost(
            20,
            method {
                returnType == "V" &&
                        parameterTypes.size == 2 &&
                        parameterTypes[0].startsWith("L") &&
                        parameterTypes[1] == "Ljava/lang/Runnable;"


            },
        ),
        afterAtMost(10, Opcode.APUT_OBJECT()),
        afterAtMost(
            5,
            method { returnType == "V" && parameterTypes.size == 1 && parameterTypes[0].startsWith("[L") },
        ),
        after(Opcode.IGET_OBJECT()),
        after(method { returnType == "I" && parameterTypes.isEmpty() }),
        after(Opcode.MOVE_RESULT()),
        after(method { returnType == "Z" && parameterTypes.size == 1 && parameterTypes[0] == "I" }),
        after(Opcode.MOVE_RESULT()),
    )
}

/**
 * Matches to the same method as [splashScreenStyleMethodMatch].
 */
internal val BytecodePatchContext.showSplashScreen2MethodMatch by composingFirstMethod {
    name("onCreate")
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
    instructions(
        allOf(
            Opcode.INVOKE_VIRTUAL(),
            method { returnType == "V" && parameterTypes.size == 1 && parameterTypes[0].startsWith("[L") }
        ),
        Opcode.IF_NE(),
        method { toString() == "Landroid/graphics/drawable/AnimatedVectorDrawable;->start()V" }
    )
}
