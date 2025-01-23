package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.util.containsLiteralInstruction
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

internal val playerSeekbarHandleColorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;")
    instructions(
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

        opcode(Opcode.FILLED_NEW_ARRAY, maxInstructionsBefore = 5),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxInstructionsBefore = 0)
    )
}

/**
 * 19.46 - 19.47
 */
internal val playerLinearGradientLegacy1946Fingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("I", "I", "I", "I")
    returns("V")
    instructions(
        resourceLiteral("color", "yt_youtube_magenta"),

        opcode(Opcode.FILLED_NEW_ARRAY),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxInstructionsBefore = 0),
    )
    custom { method, _ ->
        method.name == "setBounds"
    }
}

/**
 * 19.25 - 19.45
 */
internal val playerLinearGradientLegacy1925Fingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;")
    instructions(
        resourceLiteral("color", "yt_youtube_magenta"),

        opcode(Opcode.FILLED_NEW_ARRAY, maxInstructionsBefore = 10),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxInstructionsBefore = 0),
    )
}

internal const val launchScreenLayoutTypeLotteFeatureFlag = 268507948L

internal val launchScreenLayoutTypeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { method, _ ->
        val firstParameter = method.parameterTypes.firstOrNull()
        // 19.25 - 19.45
        (firstParameter == "Lcom/google/android/apps/youtube/app/watchwhile/MainActivity;"
                || firstParameter == "Landroid/app/Activity;") // 19.46+
                && method.containsLiteralInstruction(launchScreenLayoutTypeLotteFeatureFlag)
    }
}
