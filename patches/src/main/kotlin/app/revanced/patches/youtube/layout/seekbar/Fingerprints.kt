package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.anyInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val fullscreenSeekbarThumbnailsFingerprint by fingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    instructions(
        literal(45398577)
    )
}

internal val playerSeekbarColorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral("color", "inline_time_bar_played_not_highlighted_color"),
        resourceLiteral("color", "inline_time_bar_colorized_bar_played_color_dark")
    )
}

internal val setSeekbarClickedColorFingerprint by fingerprint {
    opcodes(Opcode.CONST_HIGH16)
    strings("YOUTUBE", "PREROLL", "POSTROLL")
    custom { _, classDef ->
        classDef.endsWith("/ControlsOverlayStyle;")
    }
}

internal val shortsSeekbarColorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral("color", "reel_time_bar_played_color")
    )
}

internal val playerSeekbarHandle1ColorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;")
    instructions(
        resourceLiteral("attr", "ytTextSecondary"),
        resourceLiteral("attr", "ytStaticBrandRed"),
    )
}

internal val playerSeekbarHandle2ColorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral("color", "inline_time_bar_live_seekable_range"),
        resourceLiteral("attr", "ytStaticBrandRed"),
    )
}

internal val watchHistoryMenuUseProgressDrawableFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    instructions(
        methodCall("Landroid/widget/ProgressBar;", "setMax"),
        opcode(Opcode.MOVE_RESULT),
        literal(-1712394514)
    )
}

internal val lithoLinearGradientFingerprint by fingerprint {
    accessFlags(AccessFlags.STATIC)
    returns("Landroid/graphics/LinearGradient;")
    parameters("F", "F", "F", "F", "[I", "[F")
}

/**
 * 19.49+
 */
internal val playerLinearGradientFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("I", "I", "I", "I", "Landroid/content/Context;", "I")
    returns("Landroid/graphics/LinearGradient;")
    instructions(
        resourceLiteral("color", "yt_youtube_magenta"),

        opcode(Opcode.FILLED_NEW_ARRAY, maxAfter = 5),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0)
    )
}

/**
 * 19.25 - 19.47
 */
internal val playerLinearGradientLegacyFingerprint by fingerprint {
    returns("V")
    instructions(
        resourceLiteral("color", "yt_youtube_magenta"),

        opcode(Opcode.FILLED_NEW_ARRAY),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0),
    )
}

internal const val launchScreenLayoutTypeLotteFeatureFlag = 268507948L

internal val launchScreenLayoutTypeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        literal(launchScreenLayoutTypeLotteFeatureFlag)
    )
    custom { method, _ ->
        val firstParameter = method.parameterTypes.firstOrNull()
        // 19.25 - 19.45
        firstParameter == "Lcom/google/android/apps/youtube/app/watchwhile/MainActivity;"
                || firstParameter == "Landroid/app/Activity;" // 19.46+
    }
}

internal val mainActivityOnCreateSplashScreenImageViewFingerprint by fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    instructions(
        methodCall(definingClass = "Landroid/widget/ImageView;", name = "getDrawable"),
        opcode(Opcode.CHECK_CAST)
    )
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/MainActivity;")
    }
}

internal const val LOTTIE_ANIMATION_VIEW_CLASS_TYPE = "Lcom/airbnb/lottie/LottieAnimationView;"

internal val lottieAnimationViewSetAnimationIntFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("I")
    returns("V")
    instructions(
        methodCall("this", "isInEditMode")
    )
    custom { _, classDef ->
        classDef.type == LOTTIE_ANIMATION_VIEW_CLASS_TYPE
    }
}

internal val lottieAnimationViewSetAnimationStreamFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("L")
    returns("V")
    instructions(
        methodCall("Ljava/util/Set;", "add"),
        literal(0)
    )
    custom { _, classDef ->
        classDef.type == LOTTIE_ANIMATION_VIEW_CLASS_TYPE
    }
}

internal val lottieCompositionFactoryZipFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("Landroid/content/Context;", "Ljava/lang/String;", "Ljava/lang/String;")
    returns("L")
    instructions(
        string(".zip"),
        string(".lottie")
    )
}

/**
 * Resolves using class found in [lottieCompositionFactoryZipFingerprint].
 *
 * [Original method](https://github.com/airbnb/lottie-android/blob/26ad8bab274eac3f93dccccfa0cafc39f7408d13/lottie/src/main/java/com/airbnb/lottie/LottieCompositionFactory.java#L386)
 */
internal val lottieCompositionFactoryFromJsonInputStreamFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("Ljava/io/InputStream;", "Ljava/lang/String;")
    returns("L")
    instructions(
        anyInstruction(literal(2), literal(3))
    )
}

