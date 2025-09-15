package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.fingerprint
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

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

internal val playerSeekbarHandle1ColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;")
    custom { method, _ ->
        method.containsLiteralInstruction(ytTextSecondaryId) &&
                method.containsLiteralInstruction(ytStaticBrandRedId)
    }
}

internal val playerSeekbarHandle2ColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;")
    custom { method, _ ->
        method.containsLiteralInstruction(inlineTimeBarLiveSeekableRangeId) &&
                method.containsLiteralInstruction(ytStaticBrandRedId)
    }
}


internal val watchHistoryMenuUseProgressDrawableFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    literal { -1712394514 }
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
    opcodes(
        Opcode.FILLED_NEW_ARRAY,
        Opcode.MOVE_RESULT_OBJECT
    )
    literal { ytYoutubeMagentaColorId }
}

/**
 * 19.25 - 19.47
 */
internal val playerLinearGradientLegacyFingerprint = fingerprint {
    returns("V")
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
        (firstParameter == YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
                || firstParameter == "Landroid/app/Activity;") // 19.46+
                && method.containsLiteralInstruction(launchScreenLayoutTypeLotteFeatureFlag)
    }
}

internal const val LOTTIE_ANIMATION_VIEW_CLASS_TYPE = "Lcom/airbnb/lottie/LottieAnimationView;"

internal val lottieAnimationViewSetAnimationIntFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("I")
    returns("V")
    custom { methodDef, classDef ->
        classDef.type == LOTTIE_ANIMATION_VIEW_CLASS_TYPE && methodDef.indexOfFirstInstruction {
            val reference = getReference<MethodReference>()
            reference?.definingClass == "Lcom/airbnb/lottie/LottieAnimationView;"
                    && reference.name == "isInEditMode"
        } >= 0
    }
}

internal val lottieAnimationViewSetAnimationStreamFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("L")
    returns("V")
    custom { methodDef, classDef ->
        classDef.type == LOTTIE_ANIMATION_VIEW_CLASS_TYPE && methodDef.indexOfFirstInstruction {
            val reference = getReference<MethodReference>()
            reference?.definingClass == "Ljava/util/Set;"
                    && reference.name == "add"
        } >= 0 && methodDef.containsLiteralInstruction(0)
    }
}

internal val lottieCompositionFactoryZipFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("Landroid/content/Context;", "Ljava/lang/String;", "Ljava/lang/String;")
    returns("L")
    strings(".zip", ".lottie")
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
    literal { 2 }
}


