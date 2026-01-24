package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.anyInstruction
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.fullscreenSeekbarThumbnailsMethod by gettingFirstMethodDeclaratively {
    returnType("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    instructions(
        45398577(),
    )
}

internal val BytecodePatchContext.playerSeekbarColorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.COLOR("inline_time_bar_played_not_highlighted_color"),
        ResourceType.COLOR("inline_time_bar_colorized_bar_played_color_dark"),
    )
}

// class is ControlsOverlayStyle in 20.32 and lower, and obfuscated in 20.33+
internal val BytecodePatchContext.setSeekbarClickedColorMethod by gettingFirstMethodDeclaratively {
    opcodes(Opcode.CONST_HIGH16)
    strings("YOUTUBE", "PREROLL", "POSTROLL", "REMOTE_LIVE", "AD_LARGE_CONTROLS")
}

internal val BytecodePatchContext.shortsSeekbarColorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.COLOR("reel_time_bar_played_color"),
    )
}

internal val BytecodePatchContext.playerSeekbarHandle1ColorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.COLOR("inline_time_bar_live_seekable_range"),
        ResourceType.ATTR("ytStaticBrandRed"),
    )
}

internal val BytecodePatchContext.playerSeekbarHandle2ColorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("Landroid/content/Context;")
    instructions(
        ResourceType.ATTR("ytTextSecondary"),
        ResourceType.ATTR("ytStaticBrandRed"),
    )
}

internal val BytecodePatchContext.watchHistoryMenuUseProgressDrawableMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        methodCall("Landroid/widget/ProgressBar;", "setMax"),
        Opcode.MOVE_RESULT(),
        -1712394514(),
    )
}

internal val BytecodePatchContext.lithoLinearGradientMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.STATIC)
    returnType("Landroid/graphics/LinearGradient;")
    parameterTypes("F", "F", "F", "F", "[I", "[F")
}

/**
 * 19.49+
 */
internal val BytecodePatchContext.playerLinearGradientMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameterTypes("I", "I", "I", "I", "Landroid/content/Context;", "I")
    returnType("Landroid/graphics/LinearGradient;")
    instructions(
        ResourceType.COLOR("yt_youtube_magenta"),

        opcode(Opcode.FILLED_NEW_ARRAY, location = MatchAfterWithin(5)),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterImmediately()),
    )
}

/**
 * 19.25 - 19.47
 */
internal val BytecodePatchContext.playerLinearGradientLegacyMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    instructions(
        ResourceType.COLOR("yt_youtube_magenta"),

        Opcode.FILLED_NEW_ARRAY(),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately()),
    )
}

internal const val LOTTIE_ANIMATION_VIEW_CLASS_TYPE = "Lcom/airbnb/lottie/LottieAnimationView;"

internal val BytecodePatchContext.lottieAnimationViewSetAnimationIntMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("I")
    returnType("V")
    instructions(
        methodCall("this", "isInEditMode"),
    )
    custom { _, classDef ->
        classDef.type == LOTTIE_ANIMATION_VIEW_CLASS_TYPE
    }
}

internal val BytecodePatchContext.lottieCompositionFactoryZipMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameterTypes("Landroid/content/Context;", "Ljava/util/zip/ZipInputStream;", "Ljava/lang/String;")
    returnType("L")
    instructions(
        addString("Unable to parse composition"),
        addString(" however it was not found in the animation."),
    )
}

/**
 * Resolves using class found in [lottieCompositionFactoryZipMethod].
 *
 * [Original method](https://github.com/airbnb/lottie-android/blob/26ad8bab274eac3f93dccccfa0cafc39f7408d13/lottie/src/main/java/com/airbnb/lottie/LottieCompositionFactory.java#L386)
 */
internal val BytecodePatchContext.lottieCompositionFactoryFromJsonInputStreamMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameterTypes("Ljava/io/InputStream;", "Ljava/lang/String;")
    returnType("L")
    instructions(
        anyInstruction(literal(2), literal(3)),
    )
}
