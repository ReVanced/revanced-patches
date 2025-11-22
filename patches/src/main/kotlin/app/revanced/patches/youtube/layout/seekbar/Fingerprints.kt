package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.anyInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val fullscreenSeekbarThumbnailsFingerprint = fingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    instructions(
        literal(45398577)
    )
}

internal val playerSeekbarColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral(ResourceType.COLOR, "inline_time_bar_played_not_highlighted_color"),
        resourceLiteral(ResourceType.COLOR, "inline_time_bar_colorized_bar_played_color_dark")
    )
}

// class is ControlsOverlayStyle in 20.32 and lower, and obfuscated in 20.33+
internal val setSeekbarClickedColorFingerprint = fingerprint {
    opcodes(Opcode.CONST_HIGH16)
    strings("YOUTUBE", "PREROLL", "POSTROLL", "REMOTE_LIVE", "AD_LARGE_CONTROLS")
}

internal val shortsSeekbarColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral(ResourceType.COLOR, "reel_time_bar_played_color")
    )
}

internal val playerSeekbarHandle1ColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral(ResourceType.COLOR, "inline_time_bar_live_seekable_range"),
        resourceLiteral(ResourceType.ATTR, "ytStaticBrandRed"),
    )
}

internal val playerSeekbarHandle2ColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;")
    instructions(
        resourceLiteral(ResourceType.ATTR, "ytTextSecondary"),
        resourceLiteral(ResourceType.ATTR, "ytStaticBrandRed"),
    )
}

internal val watchHistoryMenuUseProgressDrawableFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    instructions(
        methodCall("Landroid/widget/ProgressBar;", "setMax"),
        opcode(Opcode.MOVE_RESULT),
        literal(-1712394514)
    )
}

internal val lithoLinearGradientFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC)
    returns("Landroid/graphics/LinearGradient;")
    parameters("F", "F", "F", "F", "[I", "[F")
}

/**
 * 19.49+
 */
internal val playerLinearGradientFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("I", "I", "I", "I", "Landroid/content/Context;", "I")
    returns("Landroid/graphics/LinearGradient;")
    instructions(
        resourceLiteral(ResourceType.COLOR, "yt_youtube_magenta"),

        opcode(Opcode.FILLED_NEW_ARRAY, location = MatchAfterWithin(5)),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterImmediately())
    )
}

/**
 * 19.25 - 19.47
 */
internal val playerLinearGradientLegacyFingerprint = fingerprint {
    returns("V")
    instructions(
        resourceLiteral(ResourceType.COLOR, "yt_youtube_magenta"),

        opcode(Opcode.FILLED_NEW_ARRAY),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately()),
    )
}

internal const val LOTTIE_ANIMATION_VIEW_CLASS_TYPE = "Lcom/airbnb/lottie/LottieAnimationView;"

internal val lottieAnimationViewSetAnimationIntFingerprint = fingerprint {
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

internal val lottieCompositionFactoryZipFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("Landroid/content/Context;", "Ljava/util/zip/ZipInputStream;", "Ljava/lang/String;")
    returns("L")
    instructions(
        string("Unable to parse composition"),
        string(" however it was not found in the animation.")
    )
}

/**
 * Resolves using class found in [lottieCompositionFactoryZipFingerprint].
 *
 * [Original method](https://github.com/airbnb/lottie-android/blob/26ad8bab274eac3f93dccccfa0cafc39f7408d13/lottie/src/main/java/com/airbnb/lottie/LottieCompositionFactory.java#L386)
 */
internal val lottieCompositionFactoryFromJsonInputStreamFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("Ljava/io/InputStream;", "Ljava/lang/String;")
    returns("L")
    instructions(
        anyInstruction(literal(2), literal(3))
    )
}

