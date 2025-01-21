package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val fullscreenSeekbarThumbnailsFingerprint = fingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    literal { 45398577 }
}

internal val playerSeekbarColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { method, _ ->
        method.containsLiteralInstruction(inlineTimeBarColorizedBarPlayedColorDarkId) &&
            method.containsLiteralInstruction(inlineTimeBarPlayedNotHighlightedColorId)
    }
}

internal val setSeekbarClickedColorFingerprint = fingerprint {
    opcodes(Opcode.CONST_HIGH16)
    strings("YOUTUBE", "PREROLL", "POSTROLL")
    custom { _, classDef ->
        classDef.endsWith("ControlsOverlayStyle;")
    }
}

internal val shortsSeekbarColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    literal { reelTimeBarPlayedColorId }
}

internal val lithoLinearGradientFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC)
    returns("Landroid/graphics/LinearGradient;")
    parameters("F", "F", "F", "F", "[I", "[F")
}

/**
 * 29.25 - 19.50
 */
internal val playerLinearGradientLegacyFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("I", "I", "I", "I")
    returns("V")
    opcodes(
        Opcode.FILLED_NEW_ARRAY,
        Opcode.MOVE_RESULT_OBJECT
    )
    custom { method, _ ->
        method.name == "setBounds" && method.containsLiteralInstruction(ytYoutubeMagentaColorId)
    }
}

/**
 * 20.03+
 */
internal val playerLinearGradientFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("I", "I", "I", "I", "Landroid/content/Context;", "I")
    returns("Landroid/graphics/LinearGradient;")
    opcodes(
        Opcode.FILLED_NEW_ARRAY,
        Opcode.MOVE_RESULT_OBJECT
    )
    literal { ytYoutubeMagentaColorId }
}

internal const val launchScreenLayoutTypeLotteFeatureFlag = 268507948L

internal val launchScreenLayoutTypeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { method, _ ->
        val firstParameter = method.parameterTypes.firstOrNull()
        // 19.25 - 19.45
        (firstParameter == "Lcom/google/android/apps/youtube/app/watchwhile/MainActivity;"
                || firstParameter == "Landroid/app/Activity;") // 19.46+
                && method.containsLiteralInstruction(launchScreenLayoutTypeLotteFeatureFlag)
    }
}
