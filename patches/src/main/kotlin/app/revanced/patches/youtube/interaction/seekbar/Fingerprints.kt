package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.newInstance
import app.revanced.patcher.opcode
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_47_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_19_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_20_or_greater
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal val swipingUpGestureParentFingerprint by fingerprint {
    returns("Z")
    parameters()
    instructions(
        literal(45379021) // Swipe up fullscreen feature flag
    )
}

/**
 * Resolves using the class found in [swipingUpGestureParentFingerprint].
 */
internal val showSwipingUpGuideFingerprint by fingerprint {
    accessFlags(AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(1)
    )
}

/**
 * Resolves using the class found in [swipingUpGestureParentFingerprint].
 */
internal val allowSwipingUpGestureFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
}

internal val disableFastForwardLegacyFingerprint by fingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    // Intent start flag only used in the subscription activity
    literal {45411330}
}

internal val disableFastForwardGestureFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    opcodes(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    )
    custom { methodDef, classDef ->
        methodDef.implementation!!.instructions.count() > 30 &&
            classDef.type.endsWith("/NextGenWatchLayout;")
    }
}

internal val disableFastForwardNoticeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    )
    custom { method, _ ->
        // Code is found in different methods with different strings.
        val findSearchLandingKey = (is_19_34_or_greater && !is_19_47_or_greater)
                || is_20_19_or_greater

        method.name == "run" && method.indexOfFirstInstruction {
            val string = getReference<StringReference>()?.string
            string == "Failed to easy seek haptics vibrate."
                    || (findSearchLandingKey && string == "search_landing_cache_key")
        } >= 0
    }
}

/**
 * For 20.19 and below, this matches the same method as [disableFastForwardNoticeFingerprint].
 * For 20.20+, this matches a different method.
 */
internal val customTapAndHoldFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { method, _ ->
        // Code is found in different methods with different strings.
        val findSearchLandingKey = (is_19_34_or_greater && !is_19_47_or_greater)
                || (is_20_19_or_greater && !is_20_20_or_greater)

        method.name == "run" && method.indexOfFirstInstruction {
            val string = getReference<StringReference>()?.string
            string == "Failed to easy seek haptics vibrate."
                    || (findSearchLandingKey && string == "search_landing_cache_key")
        } >= 0
    }
}

internal val onTouchEventHandlerFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.PUBLIC)
    returns("Z")
    parameters("L")
    opcodes(
        Opcode.INVOKE_VIRTUAL, // nMethodReference
        Opcode.RETURN,
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN,
        Opcode.INT_TO_FLOAT,
        Opcode.INT_TO_FLOAT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL, // oMethodReference
    )
    custom { method, _ -> method.name == "onTouchEvent" }
}

internal val seekbarTappingFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("Landroid/view/MotionEvent;")
    instructions(
        literal(Int.MAX_VALUE),

        newInstance("Landroid/graphics/Point;"),
        methodCall(smali = "Landroid/graphics/Point;-><init>(II)V", maxAfter = 0),
        methodCall(smali = "Lj\$/util/Optional;->of(Ljava/lang/Object;)Lj\$/util/Optional;", maxAfter = 0),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0),
        fieldAccess(opcode = Opcode.IPUT_OBJECT, type = "Lj\$/util/Optional;", maxAfter = 0),

        opcode(Opcode.INVOKE_VIRTUAL, maxAfter = 10)
    )
    custom { method, _ -> method.name == "onTouchEvent" }
}

internal val slideToSeekFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;", "F")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.GOTO_16,
    )
    literal { 67108864 }
}

internal val fullscreenSeekbarThumbnailsQualityFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45399684L) // Video stream seekbar thumbnails feature flag.
    )
}
