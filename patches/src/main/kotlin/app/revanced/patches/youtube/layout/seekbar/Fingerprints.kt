package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.fullscreenSeekbarThumbnailsMethod by gettingFirstMethodDeclaratively {
    returnType("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    instructions(
        45398577L(),
    )
}

internal val playerSeekbarColorMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.COLOR("inline_time_bar_played_not_highlighted_color"),
        ResourceType.COLOR("inline_time_bar_colorized_bar_played_color_dark"),
    )
}

// class is ControlsOverlayStyle in 20.32 and lower, and obfuscated in 20.33+
internal val setSeekbarClickedColorMethodMatch = firstMethodComposite(
    "YOUTUBE",
    "PREROLL",
    "POSTROLL",
    "REMOTE_LIVE",
    "AD_LARGE_CONTROLS",
) {
    opcodes(Opcode.CONST_HIGH16)
}

internal val shortsSeekbarColorMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.COLOR("reel_time_bar_played_color"),
    )
}

internal val playerSeekbarHandle1ColorMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.COLOR("inline_time_bar_live_seekable_range"),
        ResourceType.ATTR("ytStaticBrandRed"),
    )
}

internal val playerSeekbarHandle2ColorMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("Landroid/content/Context;")
    instructions(
        ResourceType.ATTR("ytTextSecondary"),
        ResourceType.ATTR("ytStaticBrandRed"),
    )
}

internal val watchHistoryMenuUseProgressDrawableMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        method { name == "setMax" && definingClass == "Landroid/widget/ProgressBar;" },
        Opcode.MOVE_RESULT(),
        (-1712394514L)(),
    )
}

internal val BytecodePatchContext.lithoLinearGradientMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.STATIC)
    returnType("Landroid/graphics/LinearGradient;")
    parameterTypes("F", "F", "F", "F", "[I", "[F")
}

/**
 * 19.49+
 */
internal val playerLinearGradientMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameterTypes("I", "I", "I", "I", "Landroid/content/Context;", "I")
    returnType("Landroid/graphics/LinearGradient;")
    instructions(
        ResourceType.COLOR("yt_youtube_magenta"),

        afterAtMost(5, Opcode.FILLED_NEW_ARRAY()),
        after(Opcode.MOVE_RESULT_OBJECT()),
    )
}

/**
 * 19.25 - 19.47
 */
internal val playerLinearGradientLegacyMethodMatch = firstMethodComposite {
    returnType("V")
    instructions(
        ResourceType.COLOR("yt_youtube_magenta"),

        Opcode.FILLED_NEW_ARRAY(),
        after(Opcode.MOVE_RESULT_OBJECT()),
    )
}

internal const val LOTTIE_ANIMATION_VIEW_CLASS_TYPE = "Lcom/airbnb/lottie/LottieAnimationView;"

internal val BytecodePatchContext.lottieAnimationViewSetAnimationIntMethod by gettingFirstMethodDeclaratively {
    definingClass(LOTTIE_ANIMATION_VIEW_CLASS_TYPE)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("I")
    returnType("V")

    lateinit var methodDefiningClass: String
    custom {
        methodDefiningClass = definingClass
        true
    }

    instructions(method { name == "isInEditMode" && definingClass == methodDefiningClass })
}

internal val BytecodePatchContext.lottieCompositionFactoryZipMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameterTypes("Landroid/content/Context;", "Ljava/util/zip/ZipInputStream;", "Ljava/lang/String;")
    returnType("L")
    instructions(
        "Unable to parse composition"(),
        " however it was not found in the animation."(),
    )
}

/**
 * Resolves using class found in [lottieCompositionFactoryZipMethod].
 *
 * [Original method](https://github.com/airbnb/lottie-android/blob/26ad8bab274eac3f93dccccfa0cafc39f7408d13/lottie/src/main/java/com/airbnb/lottie/LottieCompositionFactory.java#L386)
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getLottieCompositionFactoryFromJsonInputStreamMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameterTypes("Ljava/io/InputStream;", "Ljava/lang/String;")
    returnType("L")
    instructions(
        anyOf(2L(), 3L()),
    )
}
